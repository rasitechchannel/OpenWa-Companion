/**
 * OpenWA engine — Baileys adapter emitting normalized domain events.
 * Media is stored as app-private file paths (never giant Base64 over the bridge).
 */
import fs from 'fs'
import path from 'path'
import http from 'http'
import { createWriteStream } from 'fs'
import { pipeline } from 'stream/promises'
import makeWASocket, {
  DisconnectReason,
  downloadMediaMessage,
  fetchLatestBaileysVersion,
  useMultiFileAuthState,
} from '@whiskeysockets/baileys'
import QRCode from 'qrcode'
import P from 'pino'
import { randomUUID } from 'crypto'

const args = process.argv.slice(2)
function argValue(flag, fallback) {
  const i = args.indexOf(flag)
  return i >= 0 ? args[i + 1] : fallback
}

const dataRoot = argValue('--openwa-data', path.join(process.cwd(), 'data'))
const accountId = argValue('--account-id', 'default')
const mediaRoot = argValue('--media-root', path.join(dataRoot, accountId, 'media'))
const authDir = path.join(dataRoot, accountId, 'auth')
const bridgeDir = path.join(process.cwd(), 'bridge')
const statusPath = path.join(bridgeDir, 'status.json')
const commandPath = path.join(bridgeDir, 'command.json')
const eventsPath = path.join(bridgeDir, 'events.ndjson')

fs.mkdirSync(authDir, { recursive: true })
fs.mkdirSync(mediaRoot, { recursive: true })
fs.mkdirSync(bridgeDir, { recursive: true })

const logger = P({ level: 'silent' })
let sock = null
let lastQr = null
let eventSeq = 0

function redact(value) {
  if (value == null) return value
  const s = typeof value === 'string' ? value : JSON.stringify(value)
  return s
    .replace(/"noiseKey"[^,}]+/g, '"noiseKey":"[REDACTED]"')
    .replace(/"signedIdentityKey"[^,}]+/g, '"signedIdentityKey":"[REDACTED]"')
    .replace(/"signedPreKey"[^,}]+/g, '"signedPreKey":"[REDACTED]"')
    .replace(/"registrationId"[^,}]+/g, '"registrationId":"[REDACTED]"')
    .replace(/"advSecretKey"[^,}]+/g, '"advSecretKey":"[REDACTED]"')
    .replace(/"me"\s*:\s*\{[^}]*\}/g, '"me":"[REDACTED_META]"')
}

let lastStatusSnapshot = {}

function writeStatus(partial) {
  let prev = { ...lastStatusSnapshot }
  try {
    const disk = JSON.parse(fs.readFileSync(statusPath, 'utf8'))
    prev = { ...disk, ...prev }
  } catch {
    /* keep memory snapshot */
  }
  // Merge so QR refresh does not wipe an issued pairingCode (and vice versa).
  // Prefer in-memory snapshot to avoid lost updates under concurrent QR + pairing writes.
  const next = {
    ok: true,
    schemaVersion: 1,
    product: 'OpenWA Companion',
    accountId,
    node: process.version,
    baileys: '6.7.24',
    connection: prev.connection || 'unknown',
    qr: Object.prototype.hasOwnProperty.call(prev, 'qr') ? prev.qr : null,
    qrDataUrl: Object.prototype.hasOwnProperty.call(prev, 'qrDataUrl') ? prev.qrDataUrl : null,
    pairingCode: Object.prototype.hasOwnProperty.call(prev, 'pairingCode') ? prev.pairingCode : null,
    me: Object.prototype.hasOwnProperty.call(prev, 'me') ? prev.me : null,
    lastError: null,
    ...partial,
    updatedAt: new Date().toISOString(),
  }
  lastStatusSnapshot = next
  fs.writeFileSync(statusPath, JSON.stringify(next, null, 2))
}

function emitDomain(eventType, payload, { classification = 'IMPLEMENTED_BACKGROUND', rawType = null } = {}) {
  eventSeq += 1
  const envelope = {
    eventId: randomUUID(),
    seq: eventSeq,
    accountId,
    eventType,
    rawType,
    classification,
    schemaVersion: 1,
    timestamp: Date.now(),
    payload,
  }
  fs.appendFileSync(eventsPath, JSON.stringify(envelope) + '\n')
  return envelope
}

function messageKey(key = {}) {
  return {
    id: key.id || null,
    remoteJid: key.remoteJid || null,
    fromMe: !!key.fromMe,
    participant: key.participant || null,
  }
}

function detectContentType(message) {
  if (!message) return 'empty'
  const keys = Object.keys(message).filter((k) => k !== 'messageContextInfo')
  return keys[0] || 'unknown'
}

function extractText(message) {
  if (!message) return null
  return (
    message.conversation ||
    message.extendedTextMessage?.text ||
    message.imageMessage?.caption ||
    message.videoMessage?.caption ||
    message.documentMessage?.caption ||
    message.documentWithCaptionMessage?.message?.documentMessage?.caption ||
    null
  )
}

function normalizeMessage(msg) {
  const contentType = detectContentType(msg.message)
  return {
    key: messageKey(msg.key),
    contentType,
    text: extractText(msg.message),
    timestamp: Number(msg.messageTimestamp || 0),
    status: msg.status ?? null,
    pushName: msg.pushName || null,
    hasMedia: /image|video|audio|document|sticker|ptv/i.test(contentType),
    quotedId: msg.message?.extendedTextMessage?.contextInfo?.stanzaId || null,
  }
}

async function maybeDownloadMedia(msg) {
  const contentType = detectContentType(msg.message)
  if (!/imageMessage|videoMessage|audioMessage|documentMessage|stickerMessage|ptvMessage|documentWithCaptionMessage/.test(contentType)) {
    return null
  }
  try {
    const buffer = await downloadMediaMessage(msg, 'buffer', {}, {
      logger,
      reuploadRequest: sock.updateMediaMessage,
    })
    const id = msg.key?.id || randomUUID()
    const ext =
      contentType.includes('image') ? 'jpg' :
      contentType.includes('video') || contentType.includes('ptv') ? 'mp4' :
      contentType.includes('audio') ? 'ogg' :
      contentType.includes('sticker') ? 'webp' : 'bin'
    const rel = path.join(accountId, `${id}.${ext}`)
    const abs = path.join(mediaRoot, `${id}.${ext}`)
    fs.writeFileSync(abs, buffer)
    return { localPath: abs, relativePath: rel, byteSize: buffer.length, contentType }
  } catch (e) {
    emitDomain('media.download_failed', {
      key: messageKey(msg.key),
      error: String(e && e.message ? e.message : e),
    }, { classification: 'CAPTURED_ONLY', rawType: 'messages.media-download' })
    return null
  }
}

function handleUnknownRaw(rawType, data) {
  emitDomain('engine.unknown_event', {
    rawType,
    preview: redact(data).slice(0, 4000),
  }, { classification: 'CAPTURED_ONLY', rawType })
}

async function processBatch(events) {
  const known = new Set([
    'connection.update', 'creds.update', 'messaging-history.set',
    'chats.upsert', 'chats.update', 'chats.phoneNumberShare', 'chats.delete',
    'presence.update', 'contacts.upsert', 'contacts.update',
    'messages.delete', 'messages.update', 'messages.media-update', 'messages.upsert',
    'messages.reaction', 'message-receipt.update',
    'groups.upsert', 'groups.update', 'group-participants.update', 'group.join-request',
    'blocklist.set', 'blocklist.update', 'call',
    'labels.edit', 'labels.association',
    'newsletter.reaction', 'newsletter.view', 'newsletter-participants.update', 'newsletter-settings.update',
  ])

  for (const rawType of Object.keys(events)) {
    if (!known.has(rawType)) {
      handleUnknownRaw(rawType, events[rawType])
    }
  }

  if (events['connection.update']) {
    const update = events['connection.update']
    const { connection, lastDisconnect, qr } = update
    if (qr) {
      lastQr = qr
      const qrDataUrl = await QRCode.toDataURL(qr, { margin: 1, width: 280 })
      writeStatus({ connection: 'qr', qr, qrDataUrl, me: sock?.user || null })
      emitDomain('connection.qr', { hasQr: true }, { classification: 'PENDING_PHYSICAL_DEVICE_TEST', rawType: 'connection.update' })
    }
    if (connection === 'open') {
      lastQr = null
      writeStatus({ connection: 'open', qr: null, qrDataUrl: null, pairingCode: null, me: sock?.user || null })
      emitDomain('connection.open', { me: sock?.user?.id || null }, { classification: 'PENDING_PHYSICAL_DEVICE_TEST', rawType: 'connection.update' })
    } else if (connection === 'close') {
      const code = lastDisconnect?.error?.output?.statusCode
      const loggedOut = code === DisconnectReason.loggedOut
      writeStatus({
        connection: loggedOut ? 'logged-out' : 'close',
        qr: null,
        qrDataUrl: null,
        // Keep issued pairing code across transient closes (408); clear only on logout/open.
        ...(loggedOut ? { pairingCode: null } : {}),
        me: null,
        lastError: code ? String(code) : String(lastDisconnect?.error || 'closed'),
      })
      emitDomain('connection.close', { code: code ?? null, loggedOut }, { classification: 'IMPLEMENTED_BACKGROUND', rawType: 'connection.update' })
      if (!loggedOut) {
        setTimeout(() => startSocket().catch((e) => writeStatus({ connection: 'error', lastError: String(e) })), 2000)
      } else {
        // Fresh pairing after upstream logout / invalid companion session.
        try {
          fs.rmSync(authDir, { recursive: true, force: true })
          fs.mkdirSync(authDir, { recursive: true })
        } catch {}
        setTimeout(() => startSocket().catch((e) => writeStatus({ connection: 'error', lastError: String(e) })), 1500)
      }
    } else if (connection) {
      // Preserve pairingCode via status merge; do not clear qrDataUrl if still waiting.
      writeStatus({ connection, me: sock?.user || null })
      emitDomain('connection.state', { connection }, { classification: 'IMPLEMENTED_BACKGROUND', rawType: 'connection.update' })
    }
  }

  if (events['creds.update']) {
    emitDomain('creds.updated', { keys: Object.keys(events['creds.update'] || {}) }, { classification: 'IMPLEMENTED_BACKGROUND', rawType: 'creds.update' })
  }

  if (events['messaging-history.set']) {
    const h = events['messaging-history.set']
    emitDomain('history.set', {
      chats: (h.chats || []).length,
      contacts: (h.contacts || []).length,
      messages: (h.messages || []).length,
      isLatest: !!h.isLatest,
      progress: h.progress ?? null,
      syncType: h.syncType ?? null,
      chatsData: (h.chats || []).map((c) => ({ id: c.id, name: c.name || c.verifiedName || null, conversationTimestamp: c.conversationTimestamp || null, unreadCount: c.unreadCount || 0, archived: !!c.archived, pinned: c.pinned || 0, muteEndTime: c.muteEndTime || null })),
      contactsData: (h.contacts || []).map((c) => ({ id: c.id, name: c.name || null, notify: c.notify || null, verifiedName: c.verifiedName || null, lid: c.lid || null })),
      messagesData: (h.messages || []).map(normalizeMessage),
    }, { classification: 'IMPLEMENTED_BACKGROUND', rawType: 'messaging-history.set' })
  }

  if (events['chats.upsert']) {
    emitDomain('chats.upsert', { chats: events['chats.upsert'] }, { classification: 'IMPLEMENTED_UI', rawType: 'chats.upsert' })
  }
  if (events['chats.update']) {
    emitDomain('chats.update', { updates: events['chats.update'] }, { classification: 'IMPLEMENTED_UI', rawType: 'chats.update' })
  }
  if (events['chats.phoneNumberShare']) {
    const p = events['chats.phoneNumberShare']
    emitDomain('lid.mapping', { lid: p.lid, jid: p.jid }, { classification: 'IMPLEMENTED_BACKGROUND', rawType: 'chats.phoneNumberShare' })
  }
  if (events['chats.delete']) {
    emitDomain('chats.delete', { ids: events['chats.delete'] }, { classification: 'IMPLEMENTED_UI', rawType: 'chats.delete' })
  }
  if (events['presence.update']) {
    emitDomain('presence.update', events['presence.update'], { classification: 'IMPLEMENTED_UI', rawType: 'presence.update' })
  }
  if (events['contacts.upsert']) {
    emitDomain('contacts.upsert', { contacts: events['contacts.upsert'] }, { classification: 'IMPLEMENTED_UI', rawType: 'contacts.upsert' })
  }
  if (events['contacts.update']) {
    emitDomain('contacts.update', { contacts: events['contacts.update'] }, { classification: 'IMPLEMENTED_UI', rawType: 'contacts.update' })
  }
  if (events['messages.upsert']) {
    const batch = events['messages.upsert']
    const normalized = []
    for (const msg of batch.messages || []) {
      const n = normalizeMessage(msg)
      if (n.hasMedia) {
        n.media = await maybeDownloadMedia(msg)
      }
      normalized.push(n)
    }
    emitDomain('messages.upsert', { type: batch.type, requestId: batch.requestId || null, messages: normalized }, { classification: 'PENDING_PHYSICAL_DEVICE_TEST', rawType: 'messages.upsert' })
  }
  if (events['messages.update']) {
    emitDomain('messages.update', { updates: events['messages.update'] }, { classification: 'IMPLEMENTED_UI', rawType: 'messages.update' })
  }
  if (events['messages.delete']) {
    emitDomain('messages.delete', events['messages.delete'], { classification: 'IMPLEMENTED_UI', rawType: 'messages.delete' })
  }
  if (events['messages.media-update']) {
    emitDomain('messages.media_update', { count: (events['messages.media-update'] || []).length }, { classification: 'IMPLEMENTED_BACKGROUND', rawType: 'messages.media-update' })
  }
  if (events['messages.reaction']) {
    emitDomain('messages.reaction', { reactions: events['messages.reaction'] }, { classification: 'IMPLEMENTED_UI', rawType: 'messages.reaction' })
  }
  if (events['message-receipt.update']) {
    emitDomain('receipts.update', { receipts: events['message-receipt.update'] }, { classification: 'IMPLEMENTED_UI', rawType: 'message-receipt.update' })
  }
  if (events['groups.upsert']) {
    emitDomain('groups.upsert', { groups: events['groups.upsert'] }, { classification: 'IMPLEMENTED_UI', rawType: 'groups.upsert' })
  }
  if (events['groups.update']) {
    emitDomain('groups.update', { updates: events['groups.update'] }, { classification: 'IMPLEMENTED_UI', rawType: 'groups.update' })
  }
  if (events['group-participants.update']) {
    emitDomain('groups.participants', events['group-participants.update'], { classification: 'IMPLEMENTED_UI', rawType: 'group-participants.update' })
  }
  if (events['group.join-request']) {
    emitDomain('groups.join_request', events['group.join-request'], { classification: 'IMPLEMENTED_UI', rawType: 'group.join-request' })
  }
  if (events['blocklist.set']) {
    emitDomain('blocklist.set', events['blocklist.set'], { classification: 'IMPLEMENTED_UI', rawType: 'blocklist.set' })
  }
  if (events['blocklist.update']) {
    emitDomain('blocklist.update', events['blocklist.update'], { classification: 'IMPLEMENTED_UI', rawType: 'blocklist.update' })
  }
  if (events.call) {
    emitDomain('calls.update', {
      calls: (events.call || []).map((c) => ({
        id: c.id,
        chatId: c.chatId,
        from: c.from,
        isGroup: !!c.isGroup,
        groupJid: c.groupJid || null,
        isVideo: !!c.isVideo,
        status: c.status,
        offline: !!c.offline,
        date: c.date ? new Date(c.date).toISOString() : null,
        liveMediaSupported: false,
      })),
    }, { classification: 'IMPLEMENTED_UI', rawType: 'call' })
  }
  if (events['labels.edit']) {
    emitDomain('labels.edit', events['labels.edit'], { classification: 'IMPLEMENTED_UI', rawType: 'labels.edit' })
  }
  if (events['labels.association']) {
    emitDomain('labels.association', events['labels.association'], { classification: 'IMPLEMENTED_UI', rawType: 'labels.association' })
  }
  if (events['newsletter.reaction']) {
    emitDomain('newsletter.reaction', events['newsletter.reaction'], { classification: 'IMPLEMENTED_UI', rawType: 'newsletter.reaction' })
  }
  if (events['newsletter.view']) {
    emitDomain('newsletter.view', events['newsletter.view'], { classification: 'CAPTURED_ONLY', rawType: 'newsletter.view' })
  }
  if (events['newsletter-participants.update']) {
    emitDomain('newsletter.participants', events['newsletter-participants.update'], { classification: 'IMPLEMENTED_UI', rawType: 'newsletter-participants.update' })
  }
  if (events['newsletter-settings.update']) {
    emitDomain('newsletter.settings', events['newsletter-settings.update'], { classification: 'CAPTURED_ONLY', rawType: 'newsletter-settings.update' })
  }
}

async function startSocket() {
  const { state, saveCreds } = await useMultiFileAuthState(authDir)
  const { version } = await fetchLatestBaileysVersion()
  sock = makeWASocket({
    version,
    auth: state,
    logger,
    printQRInTerminal: false,
    syncFullHistory: true,
    markOnlineOnConnect: false,
    generateHighQualityLinkPreview: false,
  })
  sock.ev.on('creds.update', saveCreds)
  sock.ev.process(async (events) => {
    try {
      await processBatch(events)
    } catch (e) {
      writeStatus({ lastError: String(e && e.message ? e.message : e) })
      emitDomain('engine.adapter_error', { error: String(e) }, { classification: 'CAPTURED_ONLY', rawType: 'adapter' })
    }
  })
}

async function handleCommand(cmd) {
  if (!cmd || !cmd.type) return
  const requestId = cmd.requestId || randomUUID()
  emitDomain('command.received', { requestId, type: cmd.type }, { classification: 'IMPLEMENTED_BACKGROUND', rawType: 'command' })
  switch (cmd.type) {
    case 'logout':
      if (sock) await sock.logout()
      writeStatus({ connection: 'logged-out', qr: null, qrDataUrl: null, me: null })
      break
    case 'send-text': {
      if (!sock) throw new Error('socket not ready')
      const content = { text: String(cmd.text || '') }
      if (cmd.quoted && cmd.quoted.id) {
        const quoted = {
          key: {
            id: cmd.quoted.id,
            remoteJid: cmd.quoted.remoteJid || cmd.jid,
            fromMe: !!cmd.quoted.fromMe,
            ...(cmd.quoted.participant ? { participant: cmd.quoted.participant } : {}),
          },
          message: { conversation: String(cmd.quoted.text || '') },
        }
        await sock.sendMessage(cmd.jid, content, { quoted })
      } else {
        await sock.sendMessage(cmd.jid, content)
      }
      break
    }
    case 'send-media': {
      if (!sock) throw new Error('socket not ready')
      const filePath = String(cmd.path || '')
      const mimeType = String(cmd.mimeType || 'application/octet-stream')
      if (!filePath || !fs.existsSync(filePath)) throw new Error('media file not found')
      const data = fs.readFileSync(filePath)
      const caption = String(cmd.caption || '')
      if (mimeType.startsWith('image/')) {
        await sock.sendMessage(cmd.jid, { image: data, mimetype: mimeType, caption })
      } else if (mimeType.startsWith('video/')) {
        await sock.sendMessage(cmd.jid, { video: data, mimetype: mimeType, caption })
      } else if (mimeType.startsWith('audio/')) {
        await sock.sendMessage(cmd.jid, { audio: data, mimetype: mimeType, ptt: !!cmd.ptt })
      } else {
        await sock.sendMessage(cmd.jid, {
          document: data,
          mimetype: mimeType,
          fileName: path.basename(filePath),
          caption,
        })
      }
      break
    }
    case 'send-reaction':
      if (!sock) throw new Error('socket not ready')
      await sock.sendMessage(cmd.jid, {
        react: { text: cmd.text || '', key: cmd.key },
      })
      break
    case 'delete-message':
      if (!sock) throw new Error('socket not ready')
      await sock.sendMessage(cmd.jid, { delete: cmd.key })
      break
    case 'read-messages':
      if (!sock) throw new Error('socket not ready')
      await sock.readMessages(cmd.keys || [])
      break
    case 'request-pairing-code': {
      if (!sock) throw new Error('socket not ready')
      const phone = String(cmd.phone || '').replace(/\D/g, '')
      const code = await sock.requestPairingCode(phone)
      writeStatus({ connection: 'pairing-code', pairingCode: code, me: sock.user || null })
      emitDomain(
        'connection.pairing_code',
        { issued: true, codeLength: code ? String(code).length : 0 },
        { classification: 'PENDING_PHYSICAL_DEVICE_TEST', rawType: 'requestPairingCode' },
      )
      break
    }
    case 'reject-call':
      if (!sock) throw new Error('socket not ready')
      await sock.rejectCall(cmd.callId, cmd.callFrom)
      break
    case 'chat-modify':
      if (!sock) throw new Error('socket not ready')
      await sock.chatModify(cmd.mod, cmd.jid)
      break
    case 'fetch-privacy': {
      if (!sock) throw new Error('socket not ready')
      const privacy = await sock.fetchPrivacySettings(!!cmd.force)
      emitDomain('settings.privacy', { privacy }, { classification: 'IMPLEMENTED_UI', rawType: 'fetchPrivacySettings' })
      break
    }
    default:
      emitDomain('command.unsupported', { requestId, type: cmd.type }, { classification: 'UNSUPPORTED_UPSTREAM_ACTION', rawType: cmd.type })
  }
}

function watchCommands() {
  setInterval(async () => {
    try {
      if (!fs.existsSync(commandPath)) return
      const raw = fs.readFileSync(commandPath, 'utf8')
      fs.unlinkSync(commandPath)
      await handleCommand(JSON.parse(raw))
    } catch (e) {
      writeStatus({ lastError: String(e && e.message ? e.message : e) })
    }
  }, 400)
}

function startLocalControlPort() {
  const server = http.createServer(async (req, res) => {
    if (req.method === 'GET' && req.url === '/status') {
      const body = fs.existsSync(statusPath) ? fs.readFileSync(statusPath) : Buffer.from('{}')
      res.writeHead(200, { 'content-type': 'application/json' })
      res.end(body)
      return
    }
    res.writeHead(404)
    res.end('not found')
  })
  server.listen(0, '127.0.0.1', () => {
    const { port } = server.address()
    fs.writeFileSync(path.join(bridgeDir, 'control-port.json'), JSON.stringify({ port }))
  })
}

writeStatus({ connection: 'starting' })
emitDomain('engine.boot', { accountId, mediaRoot }, { classification: 'IMPLEMENTED_BACKGROUND', rawType: 'boot' })
watchCommands()
startLocalControlPort()
startSocket().catch((e) => {
  writeStatus({ connection: 'error', lastError: String(e && e.message ? e.message : e) })
})
