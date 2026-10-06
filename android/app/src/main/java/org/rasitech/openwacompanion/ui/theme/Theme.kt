package org.rasitech.openwacompanion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class WaExtendedColors(
    val isDark: Boolean,
    val appBar: Color,
    val search: Color,
    val outgoingBubble: Color,
    val incomingBubble: Color,
    val chatWallpaper: Color,
    val tabPill: Color,
    val unread: Color,
    val fab: Color,
    val secondaryText: Color,
    val checkRead: Color,
    val checkSent: Color,
    val link: Color,
)

val LocalWaColors = staticCompositionLocalOf {
    WaExtendedColors(
        isDark = true,
        appBar = WaColor.DarkAppBar,
        search = WaColor.DarkSearch,
        outgoingBubble = WaColor.DarkOutgoing,
        incomingBubble = WaColor.DarkIncoming,
        chatWallpaper = WaColor.DarkChatWallpaper,
        tabPill = WaColor.DarkTabPill,
        unread = WaColor.Unread,
        fab = WaColor.Fab,
        secondaryText = WaColor.DarkTextSecondary,
        checkRead = WaColor.CheckRead,
        checkSent = WaColor.CheckSent,
        link = WaColor.BlueLink,
    )
}

private val LightColors = lightColorScheme(
    primary = WaColor.LightAppBar,
    onPrimary = Color.White,
    secondary = WaColor.Accent,
    onSecondary = Color.White,
    background = WaColor.LightBg,
    onBackground = WaColor.LightText,
    surface = WaColor.LightSurface,
    onSurface = WaColor.LightText,
    surfaceVariant = WaColor.LightSearch,
    onSurfaceVariant = WaColor.LightTextSecondary,
    outline = WaColor.LightDivider,
    error = WaColor.Danger,
    onError = Color.White,
    primaryContainer = WaColor.LightOutgoing,
    onPrimaryContainer = WaColor.LightText,
    secondaryContainer = WaColor.LightTabPill,
    onSecondaryContainer = WaColor.LightAppBar,
)

private val DarkColors = darkColorScheme(
    primary = WaColor.Accent,
    onPrimary = Color.Black,
    secondary = WaColor.AccentBright,
    onSecondary = Color.Black,
    background = WaColor.DarkBg,
    onBackground = WaColor.DarkText,
    surface = WaColor.DarkSurface,
    onSurface = WaColor.DarkText,
    surfaceVariant = WaColor.DarkSurface2,
    onSurfaceVariant = WaColor.DarkTextSecondary,
    outline = WaColor.DarkDivider,
    error = WaColor.Danger,
    onError = Color.White,
    primaryContainer = WaColor.DarkOutgoing,
    onPrimaryContainer = WaColor.DarkText,
    secondaryContainer = WaColor.DarkTabPill,
    onSecondaryContainer = WaColor.Accent,
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 21.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 19.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

@Composable
fun OpenWaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val extended = if (darkTheme) {
        WaExtendedColors(
            isDark = true,
            appBar = WaColor.DarkAppBar,
            search = WaColor.DarkSearch,
            outgoingBubble = WaColor.DarkOutgoing,
            incomingBubble = WaColor.DarkIncoming,
            chatWallpaper = WaColor.DarkChatWallpaper,
            tabPill = WaColor.DarkTabPill,
            unread = WaColor.Unread,
            fab = WaColor.Fab,
            secondaryText = WaColor.DarkTextSecondary,
            checkRead = WaColor.CheckRead,
            checkSent = WaColor.CheckSent,
            link = WaColor.BlueLink,
        )
    } else {
        WaExtendedColors(
            isDark = false,
            appBar = WaColor.LightAppBar,
            search = WaColor.LightSearch,
            outgoingBubble = WaColor.LightOutgoing,
            incomingBubble = WaColor.LightIncoming,
            chatWallpaper = WaColor.LightChatWallpaper,
            tabPill = WaColor.LightTabPill,
            unread = WaColor.Unread,
            fab = WaColor.Fab,
            secondaryText = WaColor.LightTextSecondary,
            checkRead = WaColor.CheckRead,
            checkSent = WaColor.CheckSent,
            link = Color(0xFF027EB5),
        )
    }
    CompositionLocalProvider(LocalWaColors provides extended) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            content = content,
        )
    }
}

object WaTheme {
    val colors: WaExtendedColors
        @Composable get() = LocalWaColors.current
}
