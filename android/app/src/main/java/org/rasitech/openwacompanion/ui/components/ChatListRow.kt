package org.rasitech.openwacompanion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.rasitech.openwacompanion.domain.model.ChatSummary
import org.rasitech.openwacompanion.ui.theme.WaDimens
import org.rasitech.openwacompanion.ui.theme.WaTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ChatListRow(
    chat: ChatSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wa = WaTheme.colors
    val unread = chat.unreadCount > 0
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = WaDimens.ChatRowMinHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = WaDimens.ListHPad, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WaAvatar(name = chat.title, size = WaDimens.AvatarList)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = chat.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Medium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatChatTime(chat.lastTimestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (unread) wa.unread else wa.secondaryText,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = chat.lastMessagePreview?.ifBlank { null }
                        ?: if (chat.isGroup) "Group" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = wa.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (unread) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(wa.unread)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                } else if (chat.mutedUntil != null && chat.mutedUntil > System.currentTimeMillis()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(wa.secondaryText.copy(alpha = 0.5f)),
                    )
                }
            }
        }
    }
}

private fun formatChatTime(epochMs: Long): String {
    if (epochMs <= 0L) return ""
    val cal = Calendar.getInstance()
    val now = Calendar.getInstance()
    cal.timeInMillis = epochMs
    return when {
        now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) ->
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))
        now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR) == 1 ->
            "Yesterday"
        now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) ->
            SimpleDateFormat("M/d/yy", Locale.getDefault()).format(Date(epochMs))
        else ->
            SimpleDateFormat("M/d/yy", Locale.getDefault()).format(Date(epochMs))
    }
}
