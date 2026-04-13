package com.swipeout.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swipeout.data.preferences.UserPreferencesRepository
import com.swipeout.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val repo: MediaRepository,
) : ViewModel() {

    /** Called immediately after the user grants gallery permission. */
    fun onPermissionGranted() {
        viewModelScope.launch { repo.sync() }
    }

    fun completeOnboarding() {
        viewModelScope.launch { prefs.setOnboardingComplete() }
    }
}
