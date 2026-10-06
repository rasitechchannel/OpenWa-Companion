package org.rasitech.openwacompanion.ui.screens.chat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.rasitech.openwacompanion.data.repo.OpenWaRepository
import org.rasitech.openwacompanion.domain.model.MessageItem
import org.rasitech.openwacompanion.ui.theme.WaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaViewerScreen(
    chatId: String,
    messageId: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    val messages by repo.observeMessages("default", chatId).collectAsStateWithLifecycle(emptyList())
    val mediaMessages = remember(messages) {
        messages.filter { !it.mediaPath.isNullOrBlank() }
    }
    var selectedIndex by remember(mediaMessages, messageId) {
        mutableIntStateOf(mediaMessages.indexOfFirst { it.messageId == messageId }.coerceAtLeast(0))
    }

    LaunchedEffect(mediaMessages.size) {
        if (mediaMessages.isNotEmpty() && selectedIndex > mediaMessages.lastIndex) {
            selectedIndex = mediaMessages.lastIndex
        }
    }

    val current = mediaMessages.getOrNull(selectedIndex)
    val wa = WaTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Media")
                        current?.let {
                            Text(
                                formatViewerTime(it.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = wa.secondaryText,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (current == null) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("Media file is not available on this device.", color = wa.secondaryText)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    MediaContent(message = current)
                }

                if (mediaMessages.size > 1) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(mediaMessages, key = { it.messageId }) { item ->
                            val index = mediaMessages.indexOfFirst { it.messageId == item.messageId }
                            MediaStripItem(
                                message = item,
                                selected = index == selectedIndex,
                                onClick = { selectedIndex = index },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaContent(message: MessageItem) {
    val path = message.mediaPath.orEmpty()
    val file = remember(path) { File(path) }
    val wa = WaTheme.colors

    when {
        !file.exists() -> {
            Text("The local media file could not be found.", color = wa.secondaryText)
        }
        isImage(message) -> {
            val bitmap = remember(path) { loadSampledBitmap(path, 1600, 1600) }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = message.text ?: "Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text("This photo could not be decoded.", color = wa.secondaryText)
            }
        }
        isVideo(message) -> {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoPath(path)
                        setOnPreparedListener { player ->
                            player.isLooping = false
                        }
                    }
                },
                update = { view ->
                    if (view.tag != path) {
                        view.tag = path
                        view.setVideoPath(path)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
        isAudio(message) -> {
            AudioPlayer(path = path, label = message.text ?: "Audio")
        }
        else -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.Description,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(file.name, style = MaterialTheme.typography.titleMedium)
                Text(formatFileSize(file.length()), color = wa.secondaryText)
            }
        }
    }
}

@Composable
private fun AudioPlayer(path: String, label: String) {
    var playing by remember(path) { mutableStateOf(false) }
    var player by remember(path) { mutableStateOf<MediaPlayer?>(null) }
    val wa = WaTheme.colors

    DisposableEffect(path) {
        onDispose {
            player?.release()
            player = null
        }
    }

    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    val active = player ?: runCatching {
                        MediaPlayer().apply {
                            setDataSource(path)
                            prepare()
                            setOnCompletionListener { playing = false }
                        }
                    }.getOrNull()?.also { player = it }
                    if (active != null) {
                        if (active.isPlaying) {
                            active.pause()
                            playing = false
                        } else {
                            active.start()
                            playing = true
                        }
                    }
                },
            ) {
                Icon(
                    if (playing) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = if (playing) "Pause" else "Play",
                )
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Local audio", color = wa.secondaryText, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun MediaStripItem(
    message: MessageItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val file = remember(message.mediaPath) { File(message.mediaPath.orEmpty()) }
    Surface(
        shape = RoundedCornerShape(10.dp),
        tonalElevation = if (selected) 3.dp else 0.dp,
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null,
        modifier = Modifier
            .size(72.dp)
            .clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                isImage(message) && file.exists() -> {
                    val bitmap = remember(message.mediaPath) {
                        loadSampledBitmap(message.mediaPath.orEmpty(), 180, 180)
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
                isVideo(message) -> Icon(Icons.Outlined.Videocam, contentDescription = null)
                isAudio(message) -> Icon(Icons.Outlined.AudioFile, contentDescription = null)
                else -> Icon(Icons.Outlined.Description, contentDescription = null)
            }
        }
    }
}

private fun isImage(message: MessageItem): Boolean =
    message.contentType.lowercase(Locale.US).contains("image") ||
        extensionOf(message.mediaPath).lowercase(Locale.US) in setOf("jpg", "jpeg", "png", "webp", "gif")

private fun isVideo(message: MessageItem): Boolean =
    message.contentType.lowercase(Locale.US).contains("video") ||
        extensionOf(message.mediaPath).lowercase(Locale.US) in setOf("mp4", "mkv", "webm", "3gp")

private fun isAudio(message: MessageItem): Boolean =
    message.contentType.lowercase(Locale.US).let { it.contains("audio") || it.contains("ptt") } ||
        extensionOf(message.mediaPath).lowercase(Locale.US) in setOf("mp3", "m4a", "aac", "ogg", "opus", "wav")

private fun extensionOf(path: String?): String =
    path?.substringAfterLast('.', missingDelimiterValue = "").orEmpty()

private fun loadSampledBitmap(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sample = 1
    while ((bounds.outWidth / sample) > reqWidth * 2 || (bounds.outHeight / sample) > reqHeight * 2) {
        sample *= 2
    }
    return BitmapFactory.decodeFile(
        path,
        BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        },
    )
}

private fun formatViewerTime(epochMs: Long): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(epochMs))

private fun formatFileSize(bytes: Long): String = when {
    bytes < 1024L -> bytes.toString() + " B"
    bytes < 1024L * 1024L -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
    else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
}
