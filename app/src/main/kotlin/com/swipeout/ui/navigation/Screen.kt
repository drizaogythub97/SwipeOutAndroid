package com.swipeout.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Onboarding  : Screen("onboarding")
    data object Home        : Screen("home")
    data object Settings    : Screen("settings")

    data object Swipe : Screen("swipe/{monthKey}") {
        fun createRoute(monthKey: String) = "swipe/$monthKey"
    }
    data object Review : Screen("review/{monthKey}") {
        fun createRoute(monthKey: String) = "review/$monthKey"
    }

    // Album-based navigation — bucketId + albumName as URL-encoded path segment
    data object AlbumSwipe : Screen("album-swipe/{bucketId}/{albumName}") {
        fun createRoute(bucketId: Long, albumName: String) =
            "album-swipe/$bucketId/${Uri.encode(albumName)}"
    }
    data object AlbumReview : Screen("album-review/{bucketId}/{albumName}") {
        fun createRoute(bucketId: Long, albumName: String) =
            "album-review/$bucketId/${Uri.encode(albumName)}"
    }
}
