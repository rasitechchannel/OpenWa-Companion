package org.rasitech.openwacompanion.ui.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.rasitech.openwacompanion.data.repo.OpenWaRepository
import org.rasitech.openwacompanion.ui.components.WaAvatar
import org.rasitech.openwacompanion.ui.components.WaEmptyState
import org.rasitech.openwacompanion.ui.util.ChatPresentation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
) {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    val contacts by repo.observeContacts("default").collectAsStateWithLifecycle(emptyList())
    var query by remember { mutableStateOf("") }

    val visible = remember(contacts, query) {
        val needle = query.trim().lowercase()
        contacts
            .filter { contact ->
                val name = contact.name ?: contact.notify ?: contact.verifiedName.orEmpty()
                needle.isBlank() ||
                    name.lowercase().contains(needle) ||
                    contact.contactId.lowercase().contains(needle)
            }
            .sortedBy { (it.name ?: it.notify ?: it.verifiedName ?: it.contactId).lowercase() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Select contact")
                        Text(
                            if (contacts.isEmpty()) "Synced contacts" else contacts.size.toString() + " contacts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Search name or number") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
            )

            if (visible.isEmpty()) {
                WaEmptyState(
                    title = if (contacts.isEmpty()) "No contacts synced yet" else "No matching contacts",
                    body = if (contacts.isEmpty()) {
                        "Contacts from your linked session will appear here after sync."
                    } else {
                        "Try another name or number."
                    },
                    icon = Icons.Outlined.Search,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn {
                    items(visible, key = { it.contactId }) { contact ->
                        val title = contact.name
                            ?: contact.notify
                            ?: contact.verifiedName
                            ?: ChatPresentation.displayTitle(null, contact.contactId)
                        ListItem(
                            headlineContent = {
                                Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            supportingContent = {
                                val subtitle = ChatPresentation.displayTitle(null, contact.contactId)
                                if (subtitle != title) {
                                    Text(
                                        subtitle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            },
                            leadingContent = { WaAvatar(name = title) },
                            modifier = Modifier.clickable { onOpenChat(contact.contactId) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
