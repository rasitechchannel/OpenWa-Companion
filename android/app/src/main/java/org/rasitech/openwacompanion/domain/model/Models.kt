package org.rasitech.openwacompanion.domain.model

enum class CapabilityStatus {
    IMPLEMENTED_UI,
    IMPLEMENTED_BACKGROUND,
    CAPTURED_ONLY,
    UNSUPPORTED_UPSTREAM_ACTION,
    NOT_APPLICABLE,
    PENDING_PHYSICAL_DEVICE_TEST,
    DEPRECATED,
}

data class Account(
    val accountId: String,
    val displayName: String?,
    val jid: String?,
    val isActive: Boolean,
    val createdAt: Long,
)

data class ChatSummary(
    val accountId: String,
    val chatId: String,
    val title: String,
    val lastMessagePreview: String?,
    val lastTimestamp: Long,
    val unreadCount: Int,
    val pinned: Boolean,
    val archived: Boolean,
    val mutedUntil: Long?,
    val isGroup: Boolean,
)

data class MessageItem(
    val accountId: String,
    val messageId: String,
    val chatId: String,
    val fromMe: Boolean,
    val senderJid: String?,
    val contentType: String,
    val text: String?,
    val timestamp: Long,
    val status: Int?,
    val mediaPath: String?,
    val quotedId: String?,
)

data class CallItem(
    val accountId: String,
    val callId: String,
    val chatId: String?,
    val fromJid: String?,
    val isVideo: Boolean,
    val status: String,
    val timestamp: Long,
    val liveMediaSupported: Boolean = false,
)
