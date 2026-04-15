package com.swipeout.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.swipeout.ui.home.HomeScreen
import com.swipeout.ui.onboarding.OnboardingScreen
import com.swipeout.ui.review.AlbumReviewScreen
import com.swipeout.ui.review.ReviewScreen
import com.swipeout.ui.settings.SettingsScreen
import com.swipeout.ui.strings.LocalStrings
import com.swipeout.ui.swipe.SwipeScreen

@Composable
fun NavGraph(
    startDestination: String = Screen.Home.route,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = startDestination,
        modifier         = modifier,
    ) {
        // ── Onboarding ────────────────────────────────────────────────────────
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                onMonthClick    = { monthKey -> navController.navigate(Screen.Swipe.createRoute(monthKey)) },
                onAlbumClick    = { bucketId, albumName ->
                    navController.navigate(Screen.AlbumSwipe.createRoute(bucketId, albumName))
                },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
            )
        }

        // ── Settings ──────────────────────────────────────────────────────────
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        // ── Month swipe ───────────────────────────────────────────────────────
        composable(
            route     = Screen.Swipe.route,
            arguments = listOf(navArgument("monthKey") { type = NavType.StringType }),
        ) { backStack ->
            val monthKey = backStack.arguments?.getString("monthKey") ?: return@composable
            val strings  = LocalStrings.current
            val title    = remember(monthKey, strings.monthNames) {
                val parts = monthKey.split("-")
                if (parts.size == 2) {
                    val monthIdx  = (parts[1].toIntOrNull() ?: 1) - 1
                    val monthName = strings.monthNames.getOrElse(monthIdx) { parts[1] }
                    "$monthName ${parts[0]}"
                } else monthKey
            }
            SwipeScreen(
                title    = title,
                onBack   = { navController.popBackStack() },
                onReview = {
                    navController.navigate(Screen.Review.createRoute(monthKey)) {
                        popUpTo(Screen.Swipe.route) { inclusive = true }
                    }
                },
            )
        }

        // ── Month review ──────────────────────────────────────────────────────
        composable(
            route     = Screen.Review.route,
            arguments = listOf(navArgument("monthKey") { type = NavType.StringType }),
        ) { backStack ->
            val monthKey = backStack.arguments?.getString("monthKey") ?: return@composable
            ReviewScreen(
                monthKey = monthKey,
                onBack   = { navController.popBackStack() },
                onDone   = { navController.popBackStack(Screen.Home.route, inclusive = false) },
            )
        }

        // ── Album swipe ───────────────────────────────────────────────────────
        composable(
            route     = Screen.AlbumSwipe.route,
            arguments = listOf(
                navArgument("bucketId")   { type = NavType.LongType },
                navArgument("albumName")  { type = NavType.StringType },
            ),
        ) { backStack ->
            val bucketId  = backStack.arguments?.getLong("bucketId")   ?: return@composable
            val albumName = backStack.arguments?.getString("albumName") ?: ""
            SwipeScreen(
                title    = albumName,
                onBack   = { navController.popBackStack() },
                onReview = {
                    navController.navigate(Screen.AlbumReview.createRoute(bucketId, albumName)) {
                        popUpTo(Screen.AlbumSwipe.route) { inclusive = true }
                    }
                },
            )
        }

        // ── Album review ──────────────────────────────────────────────────────
        composable(
            route     = Screen.AlbumReview.route,
            arguments = listOf(
                navArgument("bucketId")   { type = NavType.LongType },
                navArgument("albumName")  { type = NavType.StringType },
            ),
        ) {
            AlbumReviewScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack(Screen.Home.route, inclusive = false) },
            )
        }
    }
}
