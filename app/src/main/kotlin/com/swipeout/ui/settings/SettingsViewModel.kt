package com.swipeout.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swipeout.data.db.dao.DeletionEventDao
import com.swipeout.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Stats30(
    val filesDeleted: Long = 0L,
    val bytesFreed: Long   = 0L,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPrefs: UserPreferencesRepository,
    deletionEventDao: DeletionEventDao,
) : ViewModel() {

    val currentLanguage: StateFlow<String> = userPrefs.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "auto")

    val totalBytesFreed: StateFlow<Long> = userPrefs.totalBytesFreed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    // Reactive 30-day stats — re-emits whenever a new DeletionEvent is inserted,
    // so Settings stays in sync with review sessions without needing a manual refresh.
    // The 30-day window is anchored at VM creation; it doesn't slide during the session,
    // which keeps the underlying queries stable (acceptable — users won't leave Settings
    // open long enough to matter, and a fresh open recomputes it).
    private val since = System.currentTimeMillis() - 30L * 24 * 3600 * 1000

    val stats30: StateFlow<Stats30> = combine(
        deletionEventDao.totalFilesSinceFlow(since),
        deletionEventDao.totalBytesSinceFlow(since),
    ) { files, bytes -> Stats30(filesDeleted = files, bytesFreed = bytes) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Stats30())

    fun setLanguage(lang: String) {
        viewModelScope.launch { userPrefs.setLanguage(lang) }
    }
}
