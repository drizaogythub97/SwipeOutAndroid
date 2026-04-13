package com.swipeout.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swipeout.data.db.dao.DeletionEventDao
import com.swipeout.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val deletionEventDao: DeletionEventDao,
) : ViewModel() {

    val currentLanguage: StateFlow<String> = userPrefs.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "auto")

    val totalBytesFreed: StateFlow<Long> = userPrefs.totalBytesFreed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    private val _stats30 = MutableStateFlow(Stats30())
    val stats30: StateFlow<Stats30> = _stats30

    init {
        viewModelScope.launch {
            val since = System.currentTimeMillis() - 30L * 24 * 3600 * 1000
            _stats30.value = Stats30(
                filesDeleted = deletionEventDao.totalFilesSince(since),
                bytesFreed   = deletionEventDao.totalBytesSince(since),
            )
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch { userPrefs.setLanguage(lang) }
    }
}
