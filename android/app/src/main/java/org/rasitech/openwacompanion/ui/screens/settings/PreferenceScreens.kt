package org.rasitech.openwacompanion.ui.screens.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.SwitchAccount
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.rasitech.openwacompanion.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
) {
    val rows = listOf(
        Triple("Accounts", "Switch isolated companion sessions", Routes.AccountSwitcher.route),
        Triple("Linked devices", "View or unlink this companion session", Routes.Session.route),
        Triple("Privacy", "Synced privacy state and controls", Routes.Privacy.route),
        Triple("App lock", "Biometric or device credential", Routes.AppLock.route),
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            rows.forEachIndexed { index, row ->
                ListItem(
                    headlineContent = { Text(row.first) },
                    supportingContent = { Text(row.second) },
                    leadingContent = {
                        Icon(
                            when (index) {
                                0 -> Icons.Outlined.SwitchAccount
                                1 -> Icons.Outlined.PhoneAndroid
                                2 -> Icons.Outlined.Lock
                                else -> Icons.Outlined.Lock
                            },
                            contentDescription = null,
                        )
                    },
                    modifier = Modifier.clickable { onOpen(row.third) },
                )
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsSettingsScreen(
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
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
                headlineContent = { Text("Theme") },
                supportingContent = {
                    Text(
                        "Follows your Android light or dark theme",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Archived chats") },
                supportingContent = { Text("View chats hidden from the main list") },
                leadingContent = { Icon(Icons.Outlined.Archive, contentDescription = null) },
                modifier = Modifier.clickable { onOpen(Routes.Archived.route) },
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Storage and data") },
                supportingContent = { Text("Review local media cache") },
                leadingContent = { Icon(Icons.Outlined.Storage, contentDescription = null) },
                modifier = Modifier.clickable { onOpen(Routes.Storage.route) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
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
                headlineContent = { Text("Android notification settings") },
                supportingContent = {
                    Text(
                        "Manage permission, sound, vibration and lock-screen visibility using Android notification channels.",
                    )
                },
                leadingContent = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                modifier = Modifier.clickable {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    context.startActivity(intent)
                },
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Privacy") },
                supportingContent = {
                    Text("Device notification settings override in-app preferences.")
                },
            )
        }
    }
}
