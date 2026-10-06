package org.rasitech.openwacompanion.ui.screens.chat

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File
import org.rasitech.openwacompanion.data.repo.OpenWaRepository
import org.rasitech.openwacompanion.ui.components.MessageBubble
import org.rasitech.openwacompanion.ui.components.WaAvatar
import org.rasitech.openwacompanion.ui.theme.WaDimens
import org.rasitech.openwacompanion.ui.theme.WaTheme

@Composable
fun ConversationScreen(chatId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    val messages by repo.observeMessages("default", chatId).collectAsStateWithLifecycle(emptyList())
    val chats by repo.observeChats("default").collectAsStateWithLifecycle(emptyList())
    val title = chats.firstOrNull { it.chatId == chatId }?.title
        ?: org.rasitech.openwacompanion.ui.util.ChatPresentation.displayTitle(null, chatId)
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val wa = WaTheme.colors

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            val prepared = copyToPrivateAttachment(context, uri)
            if (prepared != null) {
                repo.sendMedia(
                    jid = chatId,
                    filePath = prepared.first,
                    mimeType = prepared.second,
                    caption = draft.trim().ifBlank { null },
                )
                draft = ""
            } else {
                Toast.makeText(context, "Could not read that attachment.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(wa.chatWallpaper)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(WaDimens.TopBarHeight)
                .background(wa.appBar)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = barIcon(wa.isDark))
            }
            WaAvatar(name = title, size = WaDimens.AvatarHeader)
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = barIcon(wa.isDark),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "No messages yet. Waiting for sync or your first send.",
                    color = wa.secondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                items(messages, key = { it.messageId }) { msg ->
                    MessageBubble(message = msg)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(WaDimens.ComposerHeight)
                    .clip(RoundedCornerShape(28.dp))
                    .background(wa.incomingBubble),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.EmojiEmotions,
                    contentDescription = null,
                    tint = wa.secondaryText,
                    modifier = Modifier.padding(start = 12.dp).size(24.dp),
                )
                TextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message", color = wa.secondaryText) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
                IconButton(onClick = { mediaPicker.launch("*/*") }) {
                    Icon(
                        Icons.Outlined.AttachFile,
                        contentDescription = "Attach",
                        tint = wa.secondaryText,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(wa.fab),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick = {
                        if (draft.isNotBlank()) {
                            repo.sendText(chatId, draft.trim())
                            draft = ""
                        } else {
                            Toast.makeText(
                                context,
                                "Voice note recording is not available in this build yet.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                ) {
                    Icon(
                        imageVector = if (draft.isBlank()) Icons.Outlined.Mic else Icons.AutoMirrored.Outlined.Send,
                        contentDescription = if (draft.isBlank()) "Voice note unavailable" else "Send",
                        tint = Color.Black,
                    )
                }
            }
        }
    }
}

private fun copyToPrivateAttachment(context: Context, uri: Uri): Pair<String, String>? {
    return runCatching {
        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val ext = when {
            mime.startsWith("image/") -> ".jpg"
            mime.startsWith("video/") -> ".mp4"
            mime.startsWith("audio/") -> ".audio"
            else -> ".bin"
        }
        val dir = File(context.cacheDir, "outgoing")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "attachment-" + System.currentTimeMillis() + ext)
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        file.absolutePath to mime
    }.getOrNull()
}

@Composable
private fun barIcon(isDark: Boolean): Color =
    if (isDark) MaterialTheme.colorScheme.onBackground else Color.White
