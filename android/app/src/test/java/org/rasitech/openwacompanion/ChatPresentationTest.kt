package org.rasitech.openwacompanion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.rasitech.openwacompanion.ui.util.ChatPresentation

class ChatPresentationTest {
    @Test
    fun cleansLiteralNullTitles() {
        assertEquals("Contact", ChatPresentation.displayTitle("null", "39307574251546@lid"))
        assertEquals("Status", ChatPresentation.displayTitle(null, "status@broadcast"))
        assertEquals("+6281234567890", ChatPresentation.displayTitle(null, "6281234567890@s.whatsapp.net"))
        assertEquals("Alice", ChatPresentation.displayTitle("Alice", "6281@s.whatsapp.net"))
    }

    @Test
    fun normalizesSecondsTimestamps() {
        val seconds = 1_759_737_600L // ~2025-10-06 UTC-ish
        val ms = ChatPresentation.normalizeEpochMs(seconds)
        assertTrue(ms > 1_000_000_000_000L)
        assertEquals(0L, ChatPresentation.normalizeEpochMs(0L))
    }

    @Test
    fun hidesGarbageChatRows() {
        assertFalse(
            ChatPresentation.includeInChatList(
                chatId = "status@broadcast",
                title = "status@broadcast",
                preview = null,
                unread = 1,
                lastTimestamp = 1_759_737_600L,
            ),
        )
        assertFalse(
            ChatPresentation.includeInChatList(
                chatId = "x@lid",
                title = "null",
                preview = null,
                unread = 0,
                lastTimestamp = 0L,
            ),
        )
        assertTrue(
            ChatPresentation.includeInChatList(
                chatId = "6281@s.whatsapp.net",
                title = "Alice",
                preview = "Hi",
                unread = 0,
                lastTimestamp = 1_759_737_600L,
            ),
        )
        assertFalse(
            ChatPresentation.includeInChatList(
                chatId = "39307574251546@lid",
                title = "39307574251546@lid",
                preview = null,
                unread = 0,
                lastTimestamp = 1_759_737_600L,
            ),
        )
        assertTrue(
            ChatPresentation.includeInChatList(
                chatId = "6281@s.whatsapp.net",
                title = "Alice",
                preview = null,
                unread = 0,
                lastTimestamp = 1_759_737_600L,
            ),
        )
    }
}
