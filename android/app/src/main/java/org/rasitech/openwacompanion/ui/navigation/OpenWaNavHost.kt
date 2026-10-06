package org.rasitech.openwacompanion.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.rasitech.openwacompanion.BuildConfig
import org.rasitech.openwacompanion.ui.screens.chat.ArchivedChatsScreen
import org.rasitech.openwacompanion.ui.screens.chat.ChatInfoScreen
import org.rasitech.openwacompanion.ui.screens.chat.ConversationScreen
import org.rasitech.openwacompanion.ui.screens.chat.NewChatScreen
import org.rasitech.openwacompanion.ui.screens.chat.MessageInfoScreen
import org.rasitech.openwacompanion.ui.screens.home.CallsTab
import org.rasitech.openwacompanion.ui.screens.home.ChatsTab
import org.rasitech.openwacompanion.ui.screens.home.CommunitiesTab
import org.rasitech.openwacompanion.ui.screens.home.UpdatesTab
import org.rasitech.openwacompanion.ui.screens.onboarding.OnboardingScreen
import org.rasitech.openwacompanion.ui.screens.onboarding.SplashScreen
import org.rasitech.openwacompanion.ui.screens.settings.AboutScreen
import org.rasitech.openwacompanion.ui.screens.settings.AccountSettingsScreen
import org.rasitech.openwacompanion.ui.screens.settings.ChatsSettingsScreen
import org.rasitech.openwacompanion.ui.screens.settings.AccountSwitcherScreen
import org.rasitech.openwacompanion.ui.screens.settings.AppLockScreen
import org.rasitech.openwacompanion.ui.screens.settings.DiagnosticsScreen
import org.rasitech.openwacompanion.ui.screens.settings.LicensesScreen
import org.rasitech.openwacompanion.ui.screens.settings.OpenSourceScreen
import org.rasitech.openwacompanion.ui.screens.settings.NotificationSettingsScreen
import org.rasitech.openwacompanion.ui.screens.settings.PrivacyScreen
import org.rasitech.openwacompanion.ui.screens.settings.SearchScreen
import org.rasitech.openwacompanion.ui.screens.settings.SessionScreen
import org.rasitech.openwacompanion.ui.screens.settings.SettingsScreen
import org.rasitech.openwacompanion.ui.screens.settings.StorageScreen
import org.rasitech.openwacompanion.ui.theme.WaDimens
import org.rasitech.openwacompanion.ui.theme.WaTheme

@Composable
fun OpenWaNavHost(
    startDestination: String = Routes.Splash.route,
) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = startDestination) {
        composable(Routes.Splash.route) {
            SplashScreen(
                onReady = {
                    nav.navigate(Routes.Onboarding.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Onboarding.route) {
            OnboardingScreen(
                onConnected = {
                    nav.navigate(Routes.Home.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSkipToHome = {
                    nav.navigate(Routes.Home.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Routes.Home.route) {
            HomeScaffold(
                onOpenChat = { nav.navigate(Routes.Conversation.create(it)) },
                onOpenSettings = { nav.navigate(Routes.Settings.route) },
                onOpenSearch = { nav.navigate(Routes.Search.route) },
                onOpenAccounts = { nav.navigate(Routes.Session.route) },
                onNewChat = { nav.navigate(Routes.NewChat.route) },
                onOpenArchived = { nav.navigate(Routes.Archived.route) },
            )
        }
        composable(Routes.NewChat.route) {
            NewChatScreen(
                onBack = { nav.popBackStack() },
                onOpenChat = { chatId ->
                    nav.navigate(Routes.Conversation.create(chatId))
                },
            )
        }
        composable(Routes.Archived.route) {
            ArchivedChatsScreen(
                onBack = { nav.popBackStack() },
                onOpenChat = { chatId -> nav.navigate(Routes.Conversation.create(chatId)) },
            )
        }
        composable(
            Routes.Conversation.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType }),
        ) { entry ->
            val chatId = entry.arguments?.getString("chatId").orEmpty()
            ConversationScreen(
                chatId = chatId,
                onBack = { nav.popBackStack() },
                onOpenInfo = { nav.navigate(Routes.ChatInfo.create(chatId)) },
                onOpenMessageInfo = { messageId ->
                    nav.navigate(Routes.MessageInfo.create(chatId, messageId))
                },
            )
        }
        composable(
            Routes.MessageInfo.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("messageId") { type = NavType.StringType },
            ),
        ) { entry ->
            MessageInfoScreen(
                chatId = entry.arguments?.getString("chatId").orEmpty(),
                messageId = entry.arguments?.getString("messageId").orEmpty(),
                onBack = { nav.popBackStack() },
            )
        }
        composable(
            Routes.ChatInfo.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType }),
        ) { entry ->
            ChatInfoScreen(
                chatId = entry.arguments?.getString("chatId").orEmpty(),
                onBack = { nav.popBackStack() },
            )
        }
        composable(Routes.Settings.route) {
            SettingsScreen(onBack = { nav.popBackStack() }, onOpen = { route -> nav.navigate(route) })
        }
        composable(Routes.AccountSettings.route) {
            AccountSettingsScreen(onBack = { nav.popBackStack() }, onOpen = { route -> nav.navigate(route) })
        }
        composable(Routes.ChatsSettings.route) {
            ChatsSettingsScreen(onBack = { nav.popBackStack() }, onOpen = { route -> nav.navigate(route) })
        }
        composable(Routes.About.route) { AboutScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.Licenses.route) { LicensesScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.OpenSource.route) { OpenSourceScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.Privacy.route) { PrivacyScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.Storage.route) { StorageScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.Notifications.route) {
            NotificationSettingsScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.AppLock.route) { AppLockScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.Session.route) { SessionScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.AccountSwitcher.route) {
            AccountSwitcherScreen(onBack = { nav.popBackStack() })
        }
        if (BuildConfig.DEBUG) {
            composable(Routes.Diagnostics.route) { DiagnosticsScreen(onBack = { nav.popBackStack() }) }
        }
        composable(Routes.Search.route) { SearchScreen(onBack = { nav.popBackStack() }) }
    }
}

@Composable
private fun HomeScaffold(
    onOpenChat: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAccounts: () -> Unit,
    onNewChat: () -> Unit,
    onOpenArchived: () -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(
        TabSpec("Chats", Icons.AutoMirrored.Outlined.Chat),
        TabSpec("Updates", Icons.Outlined.DonutLarge),
        TabSpec("Communities", Icons.Outlined.Groups),
        TabSpec("Calls", Icons.Outlined.Call),
    )
    val wa = WaTheme.colors
    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                    .navigationBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WaDimens.BottomNavHeight)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEachIndexed { index, spec ->
                        val selected = tab == index
                        NavigationBarItem(
                            selected = selected,
                            onClick = { tab = index },
                            modifier = Modifier.weight(1f),
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (wa.isDark) wa.fab else androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                unselectedIconColor = wa.secondaryText,
                                selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                                unselectedTextColor = wa.secondaryText,
                                indicatorColor = Color.Transparent,
                            ),
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (selected) wa.tabPill else Color.Transparent)
                                        .padding(horizontal = 18.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(spec.icon, contentDescription = spec.label, modifier = Modifier.size(24.dp))
                                }
                            },
                            label = {
                                Text(
                                    spec.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background),
        ) {
            when (tab) {
                0 -> ChatsTab(
                    onOpenChat = onOpenChat,
                    onOpenSettings = onOpenSettings,
                    onOpenSearch = onOpenSearch,
                    onOpenAccounts = onOpenAccounts,
                    onNewChat = onNewChat,
                    onOpenArchived = onOpenArchived,
                )
                1 -> UpdatesTab()
                2 -> CommunitiesTab()
                else -> CallsTab()
            }
        }
    }
}

private data class TabSpec(val label: String, val icon: ImageVector)
