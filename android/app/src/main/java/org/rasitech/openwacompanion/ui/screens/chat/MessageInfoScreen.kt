package org.rasitech.openwacompanion.ui.screens.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.rasitech.openwacompanion.data.repo.OpenWaRepository
import org.rasitech.openwacompanion.ui.theme.WaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInfoScreen(
    chatId: String,
    messageId: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    val messages by repo.observeMessages("default", chatId).collectAsStateWithLifecycle(emptyList())
    val receipts by repo.observeReceipts("default", messageId).collectAsStateWithLifecycle(emptyList())
    val reactions by repo.observeReactions("default", messageId).collectAsStateWithLifecycle(emptyList())
    val message = messages.firstOrNull { it.messageId == messageId }
    val wa = WaTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Message info") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ListItem(
                headlineContent = {
                    Text(message?.text?.takeIf { it.isNotBlank() } ?: message?.contentType ?: "Message")
                },
                supportingContent = {
                    Text(
                        message?.timestamp?.takeIf { it > 0L }?.let(::formatMessageInfoTime).orEmpty(),
                        color = wa.secondaryText,
                    )
                },
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text(statusTitle(message?.status)) },
                supportingContent = {
                    Text(
                        statusDescription(message?.status, receipts.size),
                        color = wa.secondaryText,
                    )
                },
            )
            HorizontalDivider()

            if (receipts.isNotEmpty()) {
                val latest = receipts.maxOfOrNull { it.timestamp } ?: 0L
                ListItem(
                    headlineContent = { Text("Receipts") },
                    supportingContent = {
                        Text(
                            receipts.size.toString() + " update" + if (receipts.size == 1) "" else "s" +
                                if (latest > 0L) " · " + formatMessageInfoTime(latest) else "",
                            color = wa.secondaryText,
                        )
                    },
                )
                HorizontalDivider()
            }

            if (reactions.isNotEmpty()) {
                val summary = reactions.mapNotNull { it.text }.groupingBy { it }.eachCount()
                    .entries.joinToString("  ") { it.key + " " + it.value }
                ListItem(
                    headlineContent = { Text("Reactions") },
                    supportingContent = { Text(summary, color = wa.secondaryText) },
                )
            }
        }
    }
}

private fun statusTitle(status: Int?): String = when {
    status == null -> "Sent"
    status >= 5 -> "Played"
    status >= 4 -> "Read"
    status >= 3 -> "Delivered"
    status >= 2 -> "Sent"
    status == 1 -> "Pending"
    else -> "Sending"
}

private fun statusDescription(status: Int?, receipts: Int): String = when {
    status != null && status >= 4 -> "The message has been read."
    status != null && status >= 3 -> "The message reached the recipient device."
    status != null && status >= 2 -> "The message was accepted by the service."
    status == 1 -> "Waiting to be sent."
    receipts > 0 -> "Receipt updates have been recorded."
    else -> "No delivery receipt has been recorded yet."
}

private fun formatMessageInfoTime(epochMs: Long): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(epochMs))
