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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material.icons.outlined.Groups
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
import org.rasitech.openwacompanion.ui.components.WaEmptyState
import org.rasitech.openwacompanion.ui.theme.WaDimens
import org.rasitech.openwacompanion.ui.theme.WaTheme
import org.rasitech.openwacompanion.ui.util.ChatPresentation

private const val ACCOUNT = "default"

@Composable
fun ChatsTab(
    onOpenChat: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAccounts: () -> Unit,
    onNewChat: () -> Unit,
    onOpenArchived: () -> Unit,
) {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    val chats by repo.observeChats(ACCOUNT).collectAsStateWithLifecycle(emptyList())
    val archived by repo.observeArchivedChats(ACCOUNT).collectAsStateWithLifecycle(emptyList())
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
                    color = if (wa.isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                )
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "More options", tint = iconTint(wa.isDark))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = WaDimens.ScreenHPad, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onOpenAccounts)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WhatsApp not linked",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Tap to pair with QR or pairing code",
                            style = MaterialTheme.typography.bodySmall,
                            color = wa.secondaryText,
                        )
                    }
                    Text(
                        text = "Link",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (archived.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenArchived)
                        .padding(horizontal = WaDimens.ListHPad, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Archive,
                        contentDescription = null,
                        tint = wa.secondaryText,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        "Archived",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f).padding(start = 20.dp),
                    )
                    val unreadArchived = archived.sumOf { it.unreadCount }
                    if (unreadArchived > 0) {
                        Text(
                            unreadArchived.toString(),
                            color = wa.unread,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            if (active.isEmpty()) {
                WaEmptyState(
                    title = if (linked) "Waiting for chats" else "No chats yet",
                    body = if (linked) {
                        "History will appear here as your linked account syncs."
                    } else {
                        "Link your WhatsApp account to see real conversations."
                    },
                    icon = Icons.Outlined.AddComment,
                    modifier = Modifier.weight(1f),
                )
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
            onClick = onNewChat,
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
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            "Updates",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = WaDimens.ScreenHPad, vertical = 12.dp),
        )
        if (channels.isEmpty()) {
            WaEmptyState(
                title = "No updates yet",
                body = "Status and channels from your linked account will show up here.",
                icon = Icons.Outlined.DonutLarge,
            )
        } else {
            Column(modifier = Modifier.padding(horizontal = WaDimens.ScreenHPad)) {
                Text("Channels", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                channels.forEach {
                    val name = ChatPresentation.cleanLabel(it.name) ?: it.newsletterId
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WaAvatar(name = name)
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                ChatPresentation.cleanLabel(it.description).orEmpty(),
                                color = wa.secondaryText,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                            )
                        }
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
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            "Communities",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = WaDimens.ScreenHPad, vertical = 12.dp),
        )
        if (groups.isEmpty()) {
            WaEmptyState(
                title = "No communities yet",
                body = "Groups from your linked session will appear here after sync.",
                icon = Icons.Outlined.Groups,
            )
        } else {
            Column(modifier = Modifier.padding(horizontal = WaDimens.ScreenHPad)) {
                groups.forEach {
                    val name = ChatPresentation.cleanLabel(it.subject) ?: it.groupId
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WaAvatar(name = name)
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                ChatPresentation.cleanLabel(it.description).orEmpty(),
                                color = wa.secondaryText,
                                maxLines = 1,
                            )
                        }
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
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            "Calls",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = WaDimens.ScreenHPad, vertical = 12.dp),
        )
        if (calls.isEmpty()) {
            WaEmptyState(
                title = "No calls yet",
                body = "Incoming call events will be listed here. Live voice and video are not supported.",
                icon = Icons.Outlined.Call,
            )
        } else {
            Column(modifier = Modifier.padding(horizontal = WaDimens.ScreenHPad)) {
                Text(
                    "Call history only — live media is not available.",
                    color = wa.secondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                calls.forEach { call ->
                    val name = ChatPresentation.displayTitle(null, call.fromJid ?: call.callId)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            WaAvatar(name = name)
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    (if (call.isVideo) "Video" else "Voice") + " · " + call.status,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(name, color = wa.secondaryText, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun iconTint(isDark: Boolean): Color =
    MaterialTheme.colorScheme.onBackground
