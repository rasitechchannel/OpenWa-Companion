package org.rasitech.openwacompanion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import org.rasitech.openwacompanion.domain.model.MessageItem
import org.rasitech.openwacompanion.ui.theme.WaDimens
import org.rasitech.openwacompanion.ui.theme.WaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: MessageItem,
    selected: Boolean = false,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val wa = WaTheme.colors
    val fromMe = message.fromMe
    val maxWidth = (LocalConfiguration.current.screenWidthDp * WaDimens.BubbleMaxFraction).dp
    val shape = if (fromMe) {
        RoundedCornerShape(topStart = 8.dp, topEnd = 2.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
    } else {
        RoundedCornerShape(topStart = 2.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
    }
    val bg = if (fromMe) wa.outgoingBubble else wa.incomingBubble
    val ts = org.rasitech.openwacompanion.ui.util.ChatPresentation.normalizeEpochMs(message.timestamp)
    val time = if (ts > 0) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))
    } else ""
    val body = message.text?.takeIf { it.isNotBlank() } ?: contentLabel(message.contentType)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else androidx.compose.ui.graphics.Color.Transparent,
            )
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick,
            )
            .padding(horizontal = 4.dp, vertical = 1.dp),
        contentAlignment = if (fromMe) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .clip(shape)
                .background(bg)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            message.quotedId?.let {
                Text(
                    text = "Reply to message",
                    style = MaterialTheme.typography.labelMedium,
                    color = wa.link,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (message.mediaPath != null && message.text.isNullOrBlank()) {
                Text(
                    text = "Saved locally",
                    style = MaterialTheme.typography.bodySmall,
                    color = wa.secondaryText,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Row(
                modifier = Modifier.align(Alignment.End).padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = time, style = MaterialTheme.typography.bodySmall, color = wa.secondaryText)
                if (fromMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ticksFor(message.status),
                        style = MaterialTheme.typography.bodySmall,
                        color = if ((message.status ?: 0) >= 4) wa.checkRead else wa.checkSent,
                    )
                }
            }
        }
    }
}

private fun contentLabel(type: String): String = when (type.lowercase(Locale.US)) {
    "image", "imagemessage" -> "Photo"
    "video", "videomessage" -> "Video"
    "audio", "audiomessage" -> "Audio"
    "ptt", "pttmessage" -> "Voice message"
    "document", "documentmessage" -> "Document"
    "sticker", "stickermessage" -> "Sticker"
    "reaction", "reactionmessage" -> "Reaction"
    "poll", "pollcreationmessage" -> "Poll"
    else -> "[$type]"
}

private fun ticksFor(status: Int?): String = when {
    status == null -> "✓"
    status >= 3 -> "✓✓"
    else -> "✓"
}
