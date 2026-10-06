package org.rasitech.openwacompanion.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.rasitech.openwacompanion.BuildConfig
import org.rasitech.openwacompanion.data.repo.OpenWaRepository
import org.rasitech.openwacompanion.security.CredentialVault
import org.rasitech.openwacompanion.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    titleOverride: String? = null,
) {
    val wa = org.rasitech.openwacompanion.ui.theme.WaTheme.colors
    val rows = buildList {
        add(SettingsRow("Account", "Accounts, linked session, security", Routes.AccountSettings.route))
        add(SettingsRow("Privacy", "Blocked accounts, disappearing messages", Routes.Privacy.route))
        add(SettingsRow("Chats", "Theme, archived chats, chat preferences", Routes.ChatsSettings.route))
        add(SettingsRow("Notifications", "Message, group & call tones", Routes.Notifications.route))
        add(SettingsRow("Storage and data", "Network usage, auto-download", Routes.Storage.route))
        add(SettingsRow("App lock", "Biometric / device credential", Routes.AppLock.route))
        add(SettingsRow("Linked devices", "Pair or manage companion session", Routes.Session.route))
        add(SettingsRow("About", "OpenWA Companion, version, disclaimer", Routes.About.route))
        add(SettingsRow("Open source", "Components used in this app", Routes.OpenSource.route))
        add(SettingsRow("Licenses", "License texts", Routes.Licenses.route))
        if (BuildConfig.DEBUG) {
            add(SettingsRow("Diagnostics", "Developer only", Routes.Diagnostics.route))
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(titleOverride ?: "Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            if (titleOverride == null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    ) {
                        org.rasitech.openwacompanion.ui.components.WaAvatar(
                            name = "OpenWA",
                            size = org.rasitech.openwacompanion.ui.theme.WaDimens.AvatarSettings,
                            brandColor = org.rasitech.openwacompanion.ui.theme.WaColor.Accent,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("OpenWA Companion", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Unofficial open-source companion client",
                            color = wa.secondaryText,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            items(rows) { row ->
                ListItem(
                    headlineContent = { Text(row.title) },
                    supportingContent = { Text(row.subtitle, color = wa.secondaryText) },
                    modifier = Modifier.clickable { onOpen(row.route) },
                )
            }
        }
    }
}

private data class SettingsRow(val title: String, val subtitle: String, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("About") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } })
    }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("OpenWA Companion", style = MaterialTheme.typography.titleLarge)
            Text("Unofficial open-source companion client")
            Text("Version " + BuildConfig.VERSION_NAME)
            Text("Not affiliated with WhatsApp LLC or Meta Platforms.")
            Text("Baileys " + BuildConfig.BAILEYS_PIN)
            Text("Embedded Node " + BuildConfig.EMBEDDED_NODE_PIN)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceScreen(onBack: () -> Unit) {
    val components = listOf(
        "WhiskeySockets/Baileys 6.7.24 (MIT)",
        "fogtape/nodejs-mobile 26.10.0-0 lite (MIT / Node notices)",
        "AndroidX / Jetpack / Compose / Room (Apache-2.0)",
        "AndroidX Security Crypto (Apache-2.0)",
        "OpenWA Dual Chat Link assets (project original)",
    )
    Scaffold(topBar = {
        TopAppBar(title = { Text("Open Source Components") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } })
    }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(components) {
                ListItem(headlineContent = { Text(it) })
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Licenses") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } })
    }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("See THIRD_PARTY_NOTICES.md and licenses/ in the project repository for full texts.")
            Text("Node.js license excerpt ships under licenses/NODE_LICENSE.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    val settings by repo.observeSettings("default").collectAsStateWithLifecycle(emptyList())
    Scaffold(topBar = {
        TopAppBar(title = { Text("Privacy") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } })
    }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { repo.fetchPrivacy() }) { Text("Refresh privacy from engine") }
            if (settings.isEmpty()) Text("No privacy payload captured yet.")
            else settings.forEach { Text(it.key + ": " + it.valueJson) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(title = { Text("Storage") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } })
    }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Media cache: " + context.cacheDir.absolutePath)
            Text("Auth (no backup): " + context.noBackupFilesDir.absolutePath)
            Text("Sensitive backup is excluded via data extraction rules.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vault = remember { CredentialVault(context) }
    var enabled by remember { mutableStateOf(vault.isAppLockEnabled()) }
    Scaffold(topBar = {
        TopAppBar(title = { Text("App lock") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } })
    }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Require device credential / biometric before opening")
                Switch(checked = enabled, onCheckedChange = {
                    enabled = it
                    vault.setAppLockEnabled(it)
                    if (!it) vault.setUnlockedSession(true)
                })
            }
            Text("Biometric prompt is requested when enabled on next cold start.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    val sync by repo.observeSync("default").collectAsStateWithLifecycle(null)
    val wa = org.rasitech.openwacompanion.ui.theme.WaTheme.colors
    val linked = sync?.connectionState == "open"
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Linked devices") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
            },
        )
    }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                if (linked) "This device is linked." else "No active linked session.",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                if (linked) {
                    "OpenWA Companion is connected as a companion device. Unlink to remove credentials from this phone."
                } else {
                    "Use the link flow from the welcome screen, or reinstall and open the app to show a QR code."
                },
                color = wa.secondaryText,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (linked) {
                Button(onClick = { repo.logout() }) { Text("Log out") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSwitcherScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    val accounts by repo.observeAccounts().collectAsStateWithLifecycle(emptyList())
    var newId by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    Scaffold(topBar = {
        TopAppBar(title = { Text("Accounts") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } })
    }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Each account uses isolated auth + media directories.")
            accounts.forEach {
                ListItem(
                    headlineContent = { Text(it.displayName ?: it.accountId) },
                    supportingContent = { Text(if (it.isActive) "Active" else "Inactive") },
                    modifier = Modifier.clickable {
                        scope.launch { repo.switchAccount(it.accountId) }
                    },
                )
            }
            OutlinedTextField(value = newId, onValueChange = { newId = it }, label = { Text("New account id") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                if (newId.isNotBlank()) scope.launch { repo.switchAccount(newId.trim()) }
            }) { Text("Add / switch account") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    val journal by repo.observeJournal("default").collectAsStateWithLifecycle(emptyList())
    Scaffold(topBar = {
        TopAppBar(title = { Text("Diagnostics") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } })
    }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(journal) {
                ListItem(
                    headlineContent = { Text(it.eventType) },
                    supportingContent = { Text(it.classification + " · " + (it.rawType ?: "-")) },
                )
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(listOf<org.rasitech.openwacompanion.data.db.entity.MessageEntity>()) }
    val scope = rememberCoroutineScope()
    Scaffold(topBar = {
        TopAppBar(title = { Text("Search") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } })
    }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Local message search") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                scope.launch { results = repo.searchMessages("default", query) }
            }) { Text("Search") }
            results.forEach {
                ListItem(headlineContent = { Text(it.text ?: it.contentType) }, supportingContent = { Text(it.chatId) })
            }
        }
    }
}