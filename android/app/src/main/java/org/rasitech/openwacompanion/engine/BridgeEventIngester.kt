package org.rasitech.openwacompanion.engine

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.rasitech.openwacompanion.data.db.OpenWaDatabase
import org.rasitech.openwacompanion.data.db.entity.CallEntity
import org.rasitech.openwacompanion.data.db.entity.CapabilityStateEntity
import org.rasitech.openwacompanion.data.db.entity.ChatEntity
import org.rasitech.openwacompanion.data.db.entity.ContactEntity
import org.rasitech.openwacompanion.data.db.entity.EventJournalEntity
import org.rasitech.openwacompanion.data.db.entity.GroupEntity
import org.rasitech.openwacompanion.data.db.entity.GroupParticipantEntity
import org.rasitech.openwacompanion.data.db.entity.JoinRequestEntity
import org.rasitech.openwacompanion.data.db.entity.LabelEntity
import org.rasitech.openwacompanion.data.db.entity.LidMappingEntity
import org.rasitech.openwacompanion.data.db.entity.MediaEntity
import org.rasitech.openwacompanion.data.db.entity.MessageEntity
import org.rasitech.openwacompanion.data.db.entity.ReactionEntity
import org.rasitech.openwacompanion.data.db.entity.ReceiptEntity
import org.rasitech.openwacompanion.data.db.entity.SettingEntity
import org.rasitech.openwacompanion.data.db.entity.SyncStateEntity
import org.rasitech.openwacompanion.domain.model.CapabilityStatus
import java.io.File
import java.io.RandomAccessFile

/**
 * Reads NDJSON domain events from the Node bridge and persists normalized state.
 * Compose UI consumes Room/domain models only — never raw Baileys payloads.
 */
class BridgeEventIngester(
    context: Context,
    private val db: OpenWaDatabase = OpenWaDatabase.get(context),
) {
    private val appContext = context.applicationContext
    private var job: Job? = null
    private var offset = 0L

    fun start(scope: CoroutineScope) {
        if (job?.isActive == true) return
        job = scope.launch(Dispatchers.IO) {
            while (isActive) {
                runCatching { drain() }.onFailure { Log.w(TAG, "ingest failed", it) }
                delay(400)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private suspend fun drain() {
        val file = File(NodeBridge.prepareRuntimeDirs(appContext).bridgeDir, "events.ndjson")
        if (!file.exists()) return
        RandomAccessFile(file, "r").use { raf ->
            if (offset > raf.length()) offset = 0L
            raf.seek(offset)
            while (true) {
                val line = raf.readLine() ?: break
                offset = raf.filePointer
                if (line.isNotBlank()) handleLine(line)
            }
        }
    }

    private suspend fun handleLine(line: String) {
        val o = JSONObject(line)
        val accountId = o.optString("accountId", "default")
        val eventType = o.optString("eventType")
        val classification = o.optString("classification", CapabilityStatus.CAPTURED_ONLY.name)
        val eventId = o.optString("eventId")
        val seq = o.optLong("seq")
        val rawType = o.optString("rawType").ifEmpty { null }
        val ts = o.optLong("timestamp", System.currentTimeMillis())
        val payload = o.optJSONObject("payload") ?: JSONObject()

        db.eventJournal().insert(
            EventJournalEntity(
                eventId = eventId,
                accountId = accountId,
                seq = seq,
                eventType = eventType,
                rawType = rawType,
                classification = classification,
                schemaVersion = o.optInt("schemaVersion", 1),
                timestamp = ts,
                payloadJson = payload.toString(),
            ),
        )

        when (eventType) {
            "history.set" -> ingestHistory(accountId, payload)
            "chats.upsert" -> ingestChats(accountId, payload.optJSONArray("chats"))
            "chats.update" -> ingestChats(accountId, payload.optJSONArray("updates"))
            "chats.delete" -> {
                val ids = payload.optJSONArray("ids").toStringList()
                if (ids.isNotEmpty()) db.chats().delete(accountId, ids)
            }
            "contacts.upsert", "contacts.update" ->
                ingestContacts(accountId, payload.optJSONArray("contacts"))
            "messages.upsert" -> ingestMessages(accountId, payload.optJSONArray("messages"))
            "messages.delete" -> ingestMessageDelete(accountId, payload)
            "messages.reaction" -> ingestReactions(accountId, payload.optJSONArray("reactions"))
            "receipts.update" -> ingestReceipts(accountId, payload.optJSONArray("receipts"))
            "groups.upsert" -> ingestGroups(accountId, payload.optJSONArray("groups"))
            "groups.participants" -> ingestParticipants(accountId, payload)
            "groups.join_request" -> ingestJoinRequest(accountId, payload)
            "lid.mapping" -> {
                val lid = payload.optString("lid")
                val jid = payload.optString("jid")
                if (lid.isNotBlank() && jid.isNotBlank()) {
                    db.lidMapping().upsert(
                        LidMappingEntity(
                            rowId = "$accountId:$lid",
                            accountId = accountId,
                            lid = lid,
                            jid = jid,
                            updatedAt = ts,
                        ),
                    )
                }
            }
            "calls.update" -> ingestCalls(accountId, payload.optJSONArray("calls"))
            "labels.edit" -> ingestLabel(accountId, payload)
            "blocklist.set", "blocklist.update", "settings.privacy" -> {
                val key = if (eventType.startsWith("blocklist")) "blocklist" else "privacy"
                db.settings().upsert(
                    SettingEntity(
                        rowId = "$accountId:$key",
                        accountId = accountId,
                        key = key,
                        valueJson = payload.toString(),
                        updatedAt = ts,
                    ),
                )
            }
            "connection.open", "connection.close", "connection.state", "connection.qr",
            "connection.pairing_code",
            -> {
                db.syncState().upsert(
                    SyncStateEntity(
                        accountId = accountId,
                        lastHistoryProgress = null,
                        lastSyncAt = ts,
                        connectionState = eventType,
                        notes = null,
                    ),
                )
            }
            else -> {
                db.capabilities().upsert(
                    CapabilityStateEntity(
                        rowId = "$accountId:$eventType",
                        accountId = accountId,
                        capabilityKey = eventType,
                        status = classification,
                        notes = "journaled",
                        updatedAt = ts,
                    ),
                )
            }
        }
    }

    private suspend fun ingestHistory(accountId: String, payload: JSONObject) {
        ingestChats(accountId, payload.optJSONArray("chatsData"))
        ingestContacts(accountId, payload.optJSONArray("contactsData"))
        ingestMessages(accountId, payload.optJSONArray("messagesData"))
        db.syncState().upsert(
            SyncStateEntity(
                accountId = accountId,
                lastHistoryProgress = payload.optInt("progress", -1).takeIf { it >= 0 },
                lastSyncAt = System.currentTimeMillis(),
                connectionState = "history",
                notes = "msgs=" + payload.optInt("messages"),
            ),
        )
    }

    private suspend fun ingestChats(accountId: String, arr: JSONArray?) {
        if (arr == null) return
        val now = System.currentTimeMillis()
        val items = buildList {
            for (i in 0 until arr.length()) {
                val c = arr.optJSONObject(i) ?: continue
                val id = c.optString("id").ifEmpty { c.optString("chatId") }
                if (id.isBlank()) continue
                add(
                    ChatEntity(
                        rowId = "$accountId:$id",
                        accountId = accountId,
                        chatId = id,
                        title = c.optString("name").ifEmpty {
                            c.optString("title").ifEmpty { id }
                        },
                        lastMessagePreview = c.optString("lastMessagePreview").ifEmpty { null },
                        lastTimestamp = c.optLong(
                            "conversationTimestamp",
                            c.optLong("lastTimestamp", 0L),
                        ),
                        unreadCount = c.optInt("unreadCount"),
                        pinned = c.optLong("pinned"),
                        archived = c.optBoolean("archived"),
                        mutedUntil = c.optLong("muteEndTime").takeIf { it > 0L },
                        isGroup = id.endsWith("@g.us"),
                        updatedAt = now,
                    ),
                )
            }
        }
        if (items.isNotEmpty()) db.chats().upsertAll(items)
    }

    private suspend fun ingestContacts(accountId: String, arr: JSONArray?) {
        if (arr == null) return
        val now = System.currentTimeMillis()
        val items = buildList {
            for (i in 0 until arr.length()) {
                val c = arr.optJSONObject(i) ?: continue
                val id = c.optString("id").ifEmpty { c.optString("contactId") }
                if (id.isBlank()) continue
                add(
                    ContactEntity(
                        rowId = "$accountId:$id",
                        accountId = accountId,
                        contactId = id,
                        name = c.optString("name").ifEmpty { null },
                        notify = c.optString("notify").ifEmpty { null },
                        verifiedName = c.optString("verifiedName").ifEmpty { null },
                        lid = c.optString("lid").ifEmpty { null },
                        updatedAt = now,
                    ),
                )
            }
        }
        if (items.isNotEmpty()) db.contacts().upsertAll(items)
    }

    private suspend fun ingestMessages(accountId: String, arr: JSONArray?) {
        if (arr == null) return
        val items = buildList {
            for (i in 0 until arr.length()) {
                val m = arr.optJSONObject(i) ?: continue
                val key = m.optJSONObject("key") ?: JSONObject()
                val messageId = key.optString("id")
                val chatId = key.optString("remoteJid")
                if (messageId.isBlank() || chatId.isBlank()) continue
                val media = m.optJSONObject("media")
                val mediaPath = media?.optString("localPath")?.ifEmpty { null }
                if (mediaPath != null) {
                    db.media().upsert(
                        MediaEntity(
                            rowId = "$accountId:$messageId",
                            accountId = accountId,
                            messageId = messageId,
                            chatId = chatId,
                            localPath = mediaPath,
                            mimeHint = media.optString("contentType").ifEmpty { null },
                            byteSize = media.optLong("byteSize"),
                            createdAt = System.currentTimeMillis(),
                        ),
                    )
                }
                add(
                    MessageEntity(
                        rowId = "$accountId:$chatId:$messageId",
                        accountId = accountId,
                        messageId = messageId,
                        chatId = chatId,
                        fromMe = key.optBoolean("fromMe"),
                        senderJid = key.optString("participant").ifEmpty { null },
                        contentType = m.optString("contentType", "unknown"),
                        text = m.optString("text").ifEmpty { null },
                        timestamp = m.optLong("timestamp"),
                        status = if (m.has("status") && !m.isNull("status")) m.optInt("status") else null,
                        mediaPath = mediaPath,
                        quotedId = m.optString("quotedId").ifEmpty { null },
                        rawJson = null,
                        deleted = false,
                    ),
                )
            }
        }
        if (items.isEmpty()) return
        db.messages().upsertAll(items)
        items.groupBy { it.chatId }.forEach { (chatId, msgs) ->
            val last = msgs.maxByOrNull { it.timestamp } ?: return@forEach
            val existing = db.chats().get(accountId, chatId)
            db.chats().upsertAll(
                listOf(
                    ChatEntity(
                        rowId = "$accountId:$chatId",
                        accountId = accountId,
                        chatId = chatId,
                        title = existing?.title ?: chatId,
                        lastMessagePreview = last.text ?: last.contentType,
                        lastTimestamp = last.timestamp,
                        unreadCount = existing?.unreadCount ?: 0,
                        pinned = existing?.pinned ?: 0L,
                        archived = existing?.archived ?: false,
                        mutedUntil = existing?.mutedUntil,
                        isGroup = chatId.endsWith("@g.us"),
                        updatedAt = System.currentTimeMillis(),
                    ),
                ),
            )
        }
    }

    private suspend fun ingestMessageDelete(accountId: String, payload: JSONObject) {
        if (payload.optBoolean("all")) return
        val keys = payload.optJSONArray("keys") ?: return
        val ids = buildList {
            for (i in 0 until keys.length()) {
                val id = keys.optJSONObject(i)?.optString("id")
                if (!id.isNullOrBlank()) add(id)
            }
        }
        if (ids.isNotEmpty()) db.messages().markDeleted(accountId, ids)
    }

    private suspend fun ingestReactions(accountId: String, arr: JSONArray?) {
        if (arr == null) return
        val items = buildList {
            for (i in 0 until arr.length()) {
                val r = arr.optJSONObject(i) ?: continue
                val key = r.optJSONObject("key") ?: continue
                val reaction = r.optJSONObject("reaction") ?: JSONObject()
                val messageId = key.optString("id")
                val chatId = key.optString("remoteJid")
                val text = reaction.optString("text")
                add(
                    ReactionEntity(
                        rowId = "$accountId:$messageId:$text:$i",
                        accountId = accountId,
                        messageId = messageId,
                        chatId = chatId,
                        reactorJid = null,
                        text = text.ifEmpty { null },
                        timestamp = System.currentTimeMillis(),
                    ),
                )
            }
        }
        if (items.isNotEmpty()) db.reactions().upsertAll(items)
    }

    private suspend fun ingestReceipts(accountId: String, arr: JSONArray?) {
        if (arr == null) return
        val items = buildList {
            for (i in 0 until arr.length()) {
                val r = arr.optJSONObject(i) ?: continue
                val key = r.optJSONObject("key") ?: continue
                val messageId = key.optString("id")
                add(
                    ReceiptEntity(
                        rowId = "$accountId:$messageId:$i",
                        accountId = accountId,
                        messageId = messageId,
                        chatId = key.optString("remoteJid"),
                        userJid = null,
                        receiptType = null,
                        timestamp = System.currentTimeMillis(),
                    ),
                )
            }
        }
        if (items.isNotEmpty()) db.receipts().upsertAll(items)
    }

    private suspend fun ingestGroups(accountId: String, arr: JSONArray?) {
        if (arr == null) return
        val now = System.currentTimeMillis()
        val items = buildList {
            for (i in 0 until arr.length()) {
                val g = arr.optJSONObject(i) ?: continue
                val id = g.optString("id")
                if (id.isBlank()) continue
                add(
                    GroupEntity(
                        rowId = "$accountId:$id",
                        accountId = accountId,
                        groupId = id,
                        subject = g.optString("subject").ifEmpty { null },
                        description = g.optString("desc").ifEmpty { null },
                        owner = g.optString("owner").ifEmpty { null },
                        creation = g.optLong("creation").takeIf { it > 0 },
                        restrict = g.optBoolean("restrict"),
                        announce = g.optBoolean("announce"),
                        updatedAt = now,
                    ),
                )
            }
        }
        if (items.isNotEmpty()) db.groups().upsertAll(items)
    }

    private suspend fun ingestParticipants(accountId: String, payload: JSONObject) {
        val groupId = payload.optString("id")
        val action = payload.optString("action")
        val participants = payload.optJSONArray("participants") ?: return
        val now = System.currentTimeMillis()
        val items = buildList {
            for (i in 0 until participants.length()) {
                val p = participants.optString(i)
                if (p.isBlank()) continue
                add(
                    GroupParticipantEntity(
                        rowId = "$accountId:$groupId:$p",
                        accountId = accountId,
                        groupId = groupId,
                        participantJid = p,
                        role = action,
                        updatedAt = now,
                    ),
                )
            }
        }
        if (items.isNotEmpty()) db.groupParticipants().upsertAll(items)
    }

    private suspend fun ingestJoinRequest(accountId: String, payload: JSONObject) {
        val groupId = payload.optString("id")
        val participant = payload.optString("participant")
        db.joinRequests().upsert(
            JoinRequestEntity(
                rowId = "$accountId:$groupId:$participant",
                accountId = accountId,
                groupId = groupId,
                participantJid = participant,
                authorJid = payload.optString("author").ifEmpty { null },
                action = payload.optString("action").ifEmpty { null },
                method = payload.optString("method").ifEmpty { null },
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    private suspend fun ingestCalls(accountId: String, arr: JSONArray?) {
        if (arr == null) return
        val items = buildList {
            for (i in 0 until arr.length()) {
                val c = arr.optJSONObject(i) ?: continue
                val id = c.optString("id")
                add(
                    CallEntity(
                        rowId = "$accountId:$id:$i",
                        accountId = accountId,
                        callId = id,
                        chatId = c.optString("chatId").ifEmpty { null },
                        fromJid = c.optString("from").ifEmpty { null },
                        isVideo = c.optBoolean("isVideo"),
                        isGroup = c.optBoolean("isGroup"),
                        status = c.optString("status"),
                        offline = c.optBoolean("offline"),
                        timestamp = System.currentTimeMillis(),
                        liveMediaSupported = false,
                    ),
                )
            }
        }
        if (items.isNotEmpty()) db.calls().upsertAll(items)
    }

    private suspend fun ingestLabel(accountId: String, payload: JSONObject) {
        val id = payload.optString("id")
        db.labels().upsert(
            LabelEntity(
                rowId = "$accountId:$id",
                accountId = accountId,
                labelId = id,
                name = payload.optString("name").ifEmpty { null },
                color = if (payload.has("color")) payload.optInt("color") else null,
                deleted = payload.optBoolean("deleted"),
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList { for (i in 0 until length()) add(optString(i)) }
    }

    companion object {
        private const val TAG = "OpenWA-Ingester"
    }
}
