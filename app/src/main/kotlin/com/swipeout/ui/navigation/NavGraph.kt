package com.swipeout.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.swipeout.ui.home.HomeScreen
import com.swipeout.ui.onboarding.OnboardingScreen
import com.swipeout.ui.review.ReviewScreen
import com.swipeout.ui.settings.SettingsScreen
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
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onMonthClick    = { monthKey -> navController.navigate(Screen.Swipe.createRoute(monthKey)) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.Swipe.route,
            arguments = listOf(navArgument("monthKey") { type = NavType.StringType }),
        ) { backStack ->
            val monthKey = backStack.arguments?.getString("monthKey") ?: return@composable
            SwipeScreen(
                monthKey = monthKey,
                onBack   = { navController.popBackStack() },
                onReview = {
                    // Pop SwipeScreen so pressing Back from ReviewScreen goes to HomeScreen,
                    // not back into SwipeScreen where isAllReviewed would re-trigger navigation.
                    navController.navigate(Screen.Review.createRoute(monthKey)) {
                        popUpTo(Screen.Swipe.route) { inclusive = true }
                    }
                },
            )
        }

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
    }
}
