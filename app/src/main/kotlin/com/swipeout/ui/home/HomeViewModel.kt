package com.swipeout.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swipeout.data.db.dao.ImageDao
import com.swipeout.data.db.entity.MonthlyMenuEntity
import com.swipeout.data.preferences.UserPreferencesRepository
import com.swipeout.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: MediaRepository,
    userPrefs: UserPreferencesRepository,
) : ViewModel() {

    val months: StateFlow<List<MonthlyMenuEntity>> = repo.months
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val albums: StateFlow<List<ImageDao.AlbumInfo>> = repo.getAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalBytesFreed: StateFlow<Long> = userPrefs.totalBytesFreed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    fun sync() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            runCatching { repo.sync() }
            _isSyncing.value = false
        }
    }
}
