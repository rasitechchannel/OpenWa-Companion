package org.rasitech.openwacompanion.data.repo

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import org.rasitech.openwacompanion.data.db.OpenWaDatabase
import org.rasitech.openwacompanion.data.db.entity.AccountEntity
import org.rasitech.openwacompanion.domain.model.Account
import org.rasitech.openwacompanion.domain.model.CallItem
import org.rasitech.openwacompanion.domain.model.ChatSummary
import org.rasitech.openwacompanion.domain.model.MessageItem
import org.rasitech.openwacompanion.engine.NodeBridge
import org.rasitech.openwacompanion.ui.util.ChatPresentation

class OpenWaRepository(context: Context) {
    private val app = context.applicationContext
    private val db = OpenWaDatabase.get(app)

    fun observeAccounts(): Flow<List<Account>> =
        db.accounts().observeAll().map { list ->
            list.map {
                Account(it.accountId, it.displayName, it.jid, it.isActive, it.createdAt)
            }
        }

    fun observeChats(accountId: String): Flow<List<ChatSummary>> =
        db.chats().observeChats(accountId).map { list ->
            list.mapNotNull { row ->
                if (!ChatPresentation.includeInChatList(
                        chatId = row.chatId,
                        title = row.title,
                        preview = row.lastMessagePreview,
                        unread = row.unreadCount,
                        lastTimestamp = row.lastTimestamp,
                    )
                ) {
                    return@mapNotNull null
                }
                ChatSummary(
                    accountId = row.accountId,
                    chatId = row.chatId,
                    title = ChatPresentation.displayTitle(row.title, row.chatId),
                    lastMessagePreview = ChatPresentation.cleanLabel(row.lastMessagePreview),
                    lastTimestamp = ChatPresentation.normalizeEpochMs(row.lastTimestamp),
                    unreadCount = row.unreadCount,
                    pinned = row.pinned > 0,
                    archived = row.archived,
                    mutedUntil = row.mutedUntil,
                    isGroup = row.isGroup,
                )
            }
        }


    fun observeArchivedChats(accountId: String): Flow<List<ChatSummary>> =
        db.chats().observeArchived(accountId).map { list ->
            list.map { row ->
                ChatSummary(
                    accountId = row.accountId,
                    chatId = row.chatId,
                    title = ChatPresentation.displayTitle(row.title, row.chatId),
                    lastMessagePreview = ChatPresentation.cleanLabel(row.lastMessagePreview),
                    lastTimestamp = ChatPresentation.normalizeEpochMs(row.lastTimestamp),
                    unreadCount = row.unreadCount,
                    pinned = row.pinned > 0,
                    archived = true,
                    mutedUntil = row.mutedUntil,
                    isGroup = row.isGroup,
                )
            }
        }

    fun observeMessages(accountId: String, chatId: String): Flow<List<MessageItem>> =
        db.messages().observeMessages(accountId, chatId).map { list ->
            list.map {
                MessageItem(
                    accountId = it.accountId,
                    messageId = it.messageId,
                    chatId = it.chatId,
                    fromMe = it.fromMe,
                    senderJid = it.senderJid,
                    contentType = it.contentType,
                    text = it.text,
                    timestamp = it.timestamp,
                    status = it.status,
                    mediaPath = it.mediaPath,
                    quotedId = it.quotedId,
                )
            }
        }

    fun observeCalls(accountId: String): Flow<List<CallItem>> =
        db.calls().observe(accountId).map { list ->
            list.map {
                CallItem(
                    accountId = it.accountId,
                    callId = it.callId,
                    chatId = it.chatId,
                    fromJid = it.fromJid,
                    isVideo = it.isVideo,
                    status = it.status,
                    timestamp = it.timestamp,
                    liveMediaSupported = it.liveMediaSupported,
                )
            }
        }

    fun observeNewsletters(accountId: String) = db.newsletters().observe(accountId)
    fun observeGroups(accountId: String) = db.groups().observe(accountId)
    fun observeGroupParticipants(accountId: String, groupId: String) =
        db.groupParticipants().observe(accountId, groupId)
    fun observeJoinRequests(accountId: String, groupId: String) =
        db.joinRequests().observe(accountId, groupId)
    fun observeJournal(accountId: String) = db.eventJournal().observeRecent(accountId)
    fun observeSync(accountId: String) = db.syncState().observe(accountId)
    fun observeSettings(accountId: String) = db.settings().observe(accountId)
    fun observeContacts(accountId: String) = db.contacts().observe(accountId)

    suspend fun ensureAccount(accountId: String) {
        val now = System.currentTimeMillis()
        db.accounts().upsert(
            AccountEntity(
                accountId = accountId,
                displayName = accountId,
                jid = null,
                isActive = true,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun switchAccount(accountId: String) {
        db.accounts().clearActive()
        ensureAccount(accountId)
        db.accounts().setActive(accountId)
    }

    suspend fun searchMessages(accountId: String, query: String) =
        db.messages().search(accountId, query)

    fun sendText(jid: String, text: String, quoted: MessageItem? = null) {
        val payload = JSONObject()
            .put("type", "send-text")
            .put("jid", jid)
            .put("text", text)
        quoted?.let {
            payload.put(
                "quoted",
                JSONObject()
                    .put("id", it.messageId)
                    .put("remoteJid", it.chatId)
                    .put("fromMe", it.fromMe)
                    .put("participant", it.senderJid ?: JSONObject.NULL)
                    .put("text", it.text ?: ""),
            )
        }
        NodeBridge.writeCommand(app, payload.toString())
    }

    fun sendReaction(message: MessageItem, emoji: String) {
        NodeBridge.writeCommand(
            app,
            JSONObject()
                .put("type", "send-reaction")
                .put("jid", message.chatId)
                .put("text", emoji)
                .put(
                    "key",
                    JSONObject()
                        .put("id", message.messageId)
                        .put("remoteJid", message.chatId)
                        .put("fromMe", message.fromMe)
                        .put("participant", message.senderJid ?: JSONObject.NULL),
                )
                .toString(),
        )
    }

    fun deleteMessage(message: MessageItem) {
        NodeBridge.writeCommand(
            app,
            JSONObject()
                .put("type", "delete-message")
                .put("jid", message.chatId)
                .put(
                    "key",
                    JSONObject()
                        .put("id", message.messageId)
                        .put("remoteJid", message.chatId)
                        .put("fromMe", message.fromMe)
                        .put("participant", message.senderJid ?: JSONObject.NULL),
                )
                .toString(),
        )
    }

    fun sendMedia(jid: String, filePath: String, mimeType: String, caption: String? = null) {
        NodeBridge.writeCommand(
            app,
            JSONObject()
                .put("type", "send-media")
                .put("jid", jid)
                .put("path", filePath)
                .put("mimeType", mimeType)
                .put("caption", caption ?: "")
                .toString(),
        )
    }

    fun requestPairingCode(phone: String) {
        NodeBridge.writeCommand(
            app,
            JSONObject().put("type", "request-pairing-code").put("phone", phone).toString(),
        )
    }

    fun logout() {
        NodeBridge.writeCommand(app, JSONObject().put("type", "logout").toString())
    }

    fun fetchPrivacy() {
        NodeBridge.writeCommand(
            app,
            JSONObject().put("type", "fetch-privacy").put("force", true).toString(),
        )
    }

    fun rejectCall(callId: String, fromJid: String) {
        NodeBridge.writeCommand(
            app,
            JSONObject()
                .put("type", "reject-call")
                .put("callId", callId)
                .put("callFrom", fromJid)
                .toString(),
        )
    }
}
