package org.rasitech.openwacompanion.ui.screens.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Groups
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.rasitech.openwacompanion.data.repo.OpenWaRepository
import org.rasitech.openwacompanion.ui.components.WaAvatar
import org.rasitech.openwacompanion.ui.components.WaEmptyState
import org.rasitech.openwacompanion.ui.theme.WaDimens
import org.rasitech.openwacompanion.ui.theme.WaTheme
import org.rasitech.openwacompanion.ui.util.ChatPresentation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInfoScreen(
    chatId: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    val chats by repo.observeChats("default").collectAsStateWithLifecycle(emptyList())
    val archived by repo.observeArchivedChats("default").collectAsStateWithLifecycle(emptyList())
    val groups by repo.observeGroups("default").collectAsStateWithLifecycle(emptyList())
    val contacts by repo.observeContacts("default").collectAsStateWithLifecycle(emptyList())
    val chat = (chats + archived).firstOrNull { it.chatId == chatId }
    val group = groups.firstOrNull { it.groupId == chatId }
    val contact = contacts.firstOrNull { it.contactId == chatId || it.lid == chatId }
    val participants by repo.observeGroupParticipants("default", chatId)
        .collectAsStateWithLifecycle(emptyList())
    val requests by repo.observeJoinRequests("default", chatId)
        .collectAsStateWithLifecycle(emptyList())
    val wa = WaTheme.colors

    val title = group?.subject
        ?: contact?.name
        ?: contact?.notify
        ?: contact?.verifiedName
        ?: chat?.title
        ?: ChatPresentation.displayTitle(null, chatId)
    val isGroup = chat?.isGroup == true || group != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isGroup) "Group info" else "Contact info") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    WaAvatar(name = title, size = WaDimens.AvatarSettings)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        if (isGroup) {
                            participants.size.toString() + " participants"
                        } else {
                            ChatPresentation.displayTitle(null, chatId)
                        },
                        color = wa.secondaryText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                HorizontalDivider()
            }

            if (isGroup) {
                group?.description?.takeIf { it.isNotBlank() }?.let { description ->
                    item {
                        ListItem(
                            headlineContent = { Text("Description") },
                            supportingContent = { Text(description) },
                        )
                        HorizontalDivider()
                    }
                }

                if (requests.isNotEmpty()) {
                    item {
                        ListItem(
                            headlineContent = { Text("Join requests") },
                            supportingContent = {
                                Text(requests.size.toString() + " pending")
                            },
                        )
                        HorizontalDivider()
                    }
                }

                item {
                    ListItem(
                        headlineContent = { Text(participants.size.toString() + " participants") },
                    )
                }

                if (participants.isEmpty()) {
                    item {
                        WaEmptyState(
                            title = "Participants not synced yet",
                            body = "Participant details will appear after group metadata sync.",
                            icon = Icons.Outlined.Groups,
                        )
                    }
                } else {
                    items(participants, key = { it.participantJid }) { participant ->
                        val display = contacts.firstOrNull {
                            it.contactId == participant.participantJid || it.lid == participant.participantJid
                        }?.let { it.name ?: it.notify ?: it.verifiedName }
                            ?: ChatPresentation.displayTitle(null, participant.participantJid)
                        ListItem(
                            headlineContent = {
                                Text(display, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            supportingContent = {
                                participant.role?.takeIf { it.isNotBlank() }?.let { Text(it) }
                            },
                            leadingContent = { WaAvatar(name = display) },
                        )
                        HorizontalDivider()
                    }
                }
            } else {
                item {
                    ListItem(
                        headlineContent = { Text("Account") },
                        supportingContent = {
                            Text(ChatPresentation.displayTitle(null, chatId))
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
