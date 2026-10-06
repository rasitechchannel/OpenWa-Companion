package org.rasitech.openwacompanion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import org.rasitech.openwacompanion.ui.theme.WaDimens
import kotlin.math.abs

private val AvatarPalette = listOf(
    Color(0xFF06CF9C),
    Color(0xFF02A698),
    Color(0xFF53BDEB),
    Color(0xFF7F66FF),
    Color(0xFFFF7A7A),
    Color(0xFFFFBC38),
    Color(0xFFC48C6A),
    Color(0xFFDF3EB1),
)

@Composable
fun WaAvatar(
    name: String,
    size: Dp = WaDimens.AvatarList,
    modifier: Modifier = Modifier,
    brandColor: Color? = null,
) {
    val letter = name.trim().firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "?"
    val color = brandColor ?: AvatarPalette[abs(name.hashCode()) % AvatarPalette.size]
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = (size.value * 0.38f).sp,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
