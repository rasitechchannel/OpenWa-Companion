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
            list.map {
                ChatSummary(
                    accountId = it.accountId,
                    chatId = it.chatId,
                    title = it.title ?: it.chatId,
                    lastMessagePreview = it.lastMessagePreview,
                    lastTimestamp = it.lastTimestamp,
                    unreadCount = it.unreadCount,
                    pinned = it.pinned > 0,
                    archived = it.archived,
                    mutedUntil = it.mutedUntil,
                    isGroup = it.isGroup,
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

    fun sendText(jid: String, text: String) {
        NodeBridge.writeCommand(
            app,
            JSONObject().put("type", "send-text").put("jid", jid).put("text", text).toString(),
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
