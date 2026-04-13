package com.swipeout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swipeout.data.preferences.UserPreferencesRepository
import com.swipeout.ui.navigation.NavGraph
import com.swipeout.ui.navigation.Screen
import com.swipeout.ui.strings.LocalStrings
import com.swipeout.ui.strings.resolveStrings
import com.swipeout.ui.theme.Background
import com.swipeout.ui.theme.SwipeOutTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPrefs: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SwipeOutTheme {
                // Observe onboarding state and language preference
                val hasSeenOnboarding by userPrefs.hasSeenOnboarding
                    .collectAsStateWithLifecycle(initialValue = null)
                val languagePref by userPrefs.appLanguage
                    .collectAsStateWithLifecycle(initialValue = "auto")

                val strings = remember(languagePref) { resolveStrings(languagePref) }

                CompositionLocalProvider(LocalStrings provides strings) {
                    when (val seen = hasSeenOnboarding) {
                        null -> {
                            // Brief loading state while DataStore initialises
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Background)
                            )
                        }
                        else -> NavGraph(
                            startDestination = if (seen) Screen.Home.route else Screen.Onboarding.route,
                            modifier         = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}
