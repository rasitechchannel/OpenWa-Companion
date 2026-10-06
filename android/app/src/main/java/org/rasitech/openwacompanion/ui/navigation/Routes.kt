package org.rasitech.openwacompanion.ui.navigation

sealed class Routes(val route: String) {
    data object Splash : Routes("splash")
    data object Onboarding : Routes("onboarding")
    data object Home : Routes("home")
    data object Search : Routes("search")
    data object Conversation : Routes("chat/{chatId}") {
        fun create(chatId: String) = "chat/$chatId"
    }
    data object Settings : Routes("settings")
    data object About : Routes("about")
    data object Licenses : Routes("licenses")
    data object OpenSource : Routes("opensource")
    data object Privacy : Routes("privacy")
    data object Storage : Routes("storage")
    data object Notifications : Routes("notifications")
    data object AppLock : Routes("applock")
    data object Session : Routes("session")
    data object AccountSwitcher : Routes("accounts")
    data object Diagnostics : Routes("diagnostics")
    data object GroupInfo : Routes("group/{groupId}") {
        fun create(groupId: String) = "group/$groupId"
    }
    data object MediaViewer : Routes("media/{path}") {
        fun create(path: String) = "media/$path"
    }
}
