package com.swipeout.ui.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home       : Screen("home")
    data object Settings   : Screen("settings")
    data object Swipe      : Screen("swipe/{monthKey}") {
        fun createRoute(monthKey: String) = "swipe/$monthKey"
    }
    data object Review     : Screen("review/{monthKey}") {
        fun createRoute(monthKey: String) = "review/$monthKey"
    }
}
