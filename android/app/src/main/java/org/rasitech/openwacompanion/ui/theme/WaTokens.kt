package org.rasitech.openwacompanion.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Visual tokens aligned to WhatsApp Android (dark + light production palettes).
 * Branding strings stay OpenWA Companion; colors/spacing match WA hierarchy only.
 */
object WaColor {
    // Dark (primary reference on current DUT WhatsApp)
    val DarkBg = Color(0xFF0B141A)
    val DarkAppBar = Color(0xFF0B141A)
    val DarkSurface = Color(0xFF1F2C34)
    val DarkSurface2 = Color(0xFF202C33)
    val DarkSearch = Color(0xFF1F2C34)
    val DarkOutgoing = Color(0xFF005C4B)
    val DarkIncoming = Color(0xFF1F2C34)
    val DarkText = Color(0xFFE9EDEF)
    val DarkTextSecondary = Color(0xFF8696A0)
    val DarkDivider = Color(0xFF222D34)
    val DarkTabPill = Color(0xFF0A3C32)
    val DarkChatWallpaper = Color(0xFF0B141A)

    // Light
    val LightBg = Color(0xFFFFFFFF)
    val LightAppBar = Color(0xFF008069)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSearch = Color(0xFFF0F2F5)
    val LightOutgoing = Color(0xFFD9FDD3)
    val LightIncoming = Color(0xFFFFFFFF)
    val LightText = Color(0xFF111B21)
    val LightTextSecondary = Color(0xFF667781)
    val LightDivider = Color(0xFFE9EDEF)
    val LightTabPill = Color(0xFFD9FDD3)
    val LightChatWallpaper = Color(0xFFEFEAE2)

    // Shared accents (WhatsApp greens — not proprietary assets)
    val Accent = Color(0xFF00A884)
    val AccentBright = Color(0xFF25D366)
    val Unread = Color(0xFF25D366)
    val Fab = Color(0xFF00A884)
    val Danger = Color(0xFFEA0038)
    val BlueLink = Color(0xFF53BDEB)
    val CheckRead = Color(0xFF53BDEB)
    val CheckSent = Color(0xFF8696A0)
}

object WaDimens {
    val AvatarList = 49.dp
    val AvatarHeader = 40.dp
    val AvatarSettings = 88.dp
    val ChatRowMinHeight = 72.dp
    val ScreenHPad = 16.dp
    val ListHPad = 12.dp
    val BubbleMaxFraction = 0.82f
    val BubbleRadius = 8.dp
    val ComposerHeight = 52.dp
    val FabSize = 56.dp
    val SearchHeight = 48.dp
    val BottomNavHeight = 72.dp
    val TopBarHeight = 56.dp
}
