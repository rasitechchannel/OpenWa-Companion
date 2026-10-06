package org.rasitech.openwacompanion.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.rasitech.openwacompanion.data.repo.OpenWaRepository
import org.rasitech.openwacompanion.ui.components.ChatListRow
import org.rasitech.openwacompanion.ui.components.WaAvatar
import org.rasitech.openwacompanion.ui.theme.WaDimens
import org.rasitech.openwacompanion.ui.theme.WaTheme

private const val ACCOUNT = "default"

@Composable
fun ChatsTab(
    onOpenChat: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAccounts: () -> Unit,
) {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    val chats by repo.observeChats(ACCOUNT).collectAsStateWithLifecycle(emptyList())
    val sync by repo.observeSync(ACCOUNT).collectAsStateWithLifecycle(null)
    val wa = WaTheme.colors
    var menuOpen by remember { mutableStateOf(false) }
    val linked = sync?.connectionState == "open"
    val active = chats.filter { !it.archived }
    val pinned = active.filter { it.pinned }
    val normal = active.filter { !it.pinned }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(wa.appBar)
                    .height(WaDimens.TopBarHeight)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "OpenWA",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (wa.isDark) MaterialTheme.colorScheme.onBackground else Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                )
                IconButton(onClick = { /* camera attach reserved; no fake capture */ }) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = "Camera", tint = iconTint(wa.isDark))
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "More options", tint = iconTint(wa.isDark))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("New group") }, onClick = { menuOpen = false })
                        DropdownMenuItem(
                            text = { Text("Linked devices") },
                            onClick = { menuOpen = false; onOpenAccounts() },
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = { menuOpen = false; onOpenSettings() },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WaDimens.ScreenHPad, vertical = 6.dp)
                    .height(WaDimens.SearchHeight)
                    .clip(RoundedCornerShape(28.dp))
                    .background(wa.search)
                    .clickable(onClick = onOpenSearch)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = wa.secondaryText, modifier = Modifier.size(22.dp))
                Text(
                    text = "Search",
                    color = wa.secondaryText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }

            if (!linked) {
                Text(
                    text = "Not linked — open Settings → Linked devices to pair. No dummy chats.",
                    style = MaterialTheme.typography.bodySmall,
                    color = wa.secondaryText,
                    modifier = Modifier.padding(horizontal = WaDimens.ScreenHPad, vertical = 8.dp),
                )
            }

            if (active.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Chats", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (linked) {
                                "Waiting for history sync from your linked account."
                            } else {
                                "Link your WhatsApp account to see real conversations."
                            },
                            color = wa.secondaryText,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
                    if (pinned.isNotEmpty()) {
                        items(pinned, key = { "p-" + it.chatId }) { chat ->
                            ChatListRow(chat = chat, onClick = { onOpenChat(chat.chatId) })
                        }
                    }
                    items(normal, key = { it.chatId }) { chat ->
                        ChatListRow(chat = chat, onClick = { onOpenChat(chat.chatId) })
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onOpenSearch,
            containerColor = wa.fab,
            contentColor = Color.Black,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
        ) {
            Icon(Icons.Outlined.AddComment, contentDescription = "New chat")
        }
    }
}

@Composable
fun UpdatesTab() {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    val channels by repo.observeNewsletters(ACCOUNT).collectAsStateWithLifecycle(emptyList())
    val wa = WaTheme.colors
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(WaDimens.ScreenHPad)) {
        Text("Updates", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Status", style = MaterialTheme.typography.titleMedium)
        Text(
            "Status updates appear here when synced. Nothing is fabricated.",
            color = wa.secondaryText,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("Channels", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        if (channels.isEmpty()) {
            Text("No channels yet.", color = wa.secondaryText)
        } else {
            channels.forEach {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WaAvatar(name = it.name ?: it.newsletterId)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(it.name ?: it.newsletterId, style = MaterialTheme.typography.titleMedium)
                        Text(it.description ?: "", color = wa.secondaryText, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun CommunitiesTab() {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    val groups by repo.observeGroups(ACCOUNT).collectAsStateWithLifecycle(emptyList())
    val wa = WaTheme.colors
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(WaDimens.ScreenHPad)) {
        Text("Communities", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Groups and community metadata from your linked session.",
            color = wa.secondaryText,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (groups.isEmpty()) {
            Text("No communities or groups synced yet.", color = wa.secondaryText)
        } else {
            groups.forEach {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WaAvatar(name = it.subject ?: it.groupId)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(it.subject ?: it.groupId, style = MaterialTheme.typography.titleMedium)
                        Text(it.description ?: "", color = wa.secondaryText, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun CallsTab() {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    val calls by repo.observeCalls(ACCOUNT).collectAsStateWithLifecycle(emptyList())
    val wa = WaTheme.colors
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(WaDimens.ScreenHPad)) {
        Text("Calls", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Call events only. Live voice/video media is not supported.",
            color = wa.secondaryText,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (calls.isEmpty()) {
            Text("No call events yet.", color = wa.secondaryText)
        } else {
            calls.forEach { call ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        WaAvatar(name = call.fromJid ?: call.callId)
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                (if (call.isVideo) "Video" else "Voice") + " · " + call.status,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(call.fromJid ?: call.chatId ?: call.callId, color = wa.secondaryText, maxLines = 1)
                        }
                    }
                    Text("No live media", color = wa.secondaryText, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun iconTint(isDark: Boolean): Color =
    if (isDark) MaterialTheme.colorScheme.onBackground else Color.White
