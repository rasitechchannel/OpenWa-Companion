package org.rasitech.openwacompanion.ui.util

/**
 * Presentation helpers so Room/Baileys quirks never leak into the UI as "null" / 1970 dates.
 */
object ChatPresentation {
    fun cleanLabel(raw: String?): String? {
        val v = raw?.trim().orEmpty()
        if (v.isEmpty()) return null
        if (v.equals("null", ignoreCase = true) || v.equals("undefined", ignoreCase = true)) return null
        return v
    }

    fun normalizeEpochMs(value: Long): Long {
        if (value <= 0L) return 0L
        // Baileys conversationTimestamp is often seconds; UI expects millis.
        return if (value < 1_000_000_000_000L) value * 1000L else value
    }

    fun displayTitle(title: String?, chatId: String): String {
        val cleaned = cleanLabel(title)
        if (cleaned != null && !looksLikeRawAddress(cleaned)) return cleaned
        return friendlyFromChatId(chatId)
    }

    fun friendlyFromChatId(chatId: String): String {
        val id = chatId.trim()
        return when {
            id.equals("status@broadcast", ignoreCase = true) -> "Status"
            id.endsWith("@broadcast", ignoreCase = true) -> "Broadcast"
            id.endsWith("@g.us", ignoreCase = true) -> "Group"
            id.endsWith("@newsletter", ignoreCase = true) -> "Channel"
            id.endsWith("@lid", ignoreCase = true) -> "Contact"
            id.contains("@") -> {
                val user = id.substringBefore("@")
                when {
                    user.isBlank() -> "Chat"
                    user.all { it.isDigit() } && user.length >= 8 -> "+$user"
                    else -> user
                }
            }
            id.isBlank() -> "Chat"
            else -> id
        }
    }

    /** Hide protocol / garbage rows from the main chat list. */
    fun includeInChatList(
        chatId: String,
        title: String?,
        preview: String?,
        unread: Int,
        lastTimestamp: Long,
    ): Boolean {
        if (chatId.equals("status@broadcast", ignoreCase = true)) return false
        if (chatId.endsWith("@broadcast", ignoreCase = true)) return false
        val cleaned = cleanLabel(title)
        val hasPreview = !preview.isNullOrBlank() && cleanLabel(preview) != null
        if (hasPreview || unread > 0) return true
        // No preview/unread: only keep rows with a human-readable name (not raw JID / null).
        if (cleaned == null) return false
        if (looksLikeRawAddress(cleaned)) return false
        if (cleaned.equals(chatId, ignoreCase = true)) return false
        // Drop leftover zero-activity placeholders even if a label sneaks through.
        if (normalizeEpochMs(lastTimestamp) <= 0L) return false
        return true
    }

    private fun looksLikeRawAddress(value: String): Boolean {
        if (value.contains('@')) return true
        if (value.endsWith(".us", ignoreCase = true)) return true
        return false
    }
}
