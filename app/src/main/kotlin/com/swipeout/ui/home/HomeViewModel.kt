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

enum class AlbumSortOrder { ALPHA, MOST_FILES, LEAST_FILES }

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: MediaRepository,
    userPrefs: UserPreferencesRepository,
) : ViewModel() {

    val months: StateFlow<List<MonthlyMenuEntity>> = repo.months
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _albumSort = MutableStateFlow(AlbumSortOrder.ALPHA)
    val albumSort: StateFlow<AlbumSortOrder> = _albumSort

    val albums: StateFlow<List<ImageDao.AlbumInfo>> =
        combine(repo.getAlbums(), _albumSort) { list, sort ->
            when (sort) {
                AlbumSortOrder.ALPHA       -> list
                AlbumSortOrder.MOST_FILES  -> list.sortedByDescending { it.totalCount }
                AlbumSortOrder.LEAST_FILES -> list.sortedBy { it.totalCount }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalBytesFreed: StateFlow<Long> = userPrefs.totalBytesFreed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    fun setAlbumSort(order: AlbumSortOrder) { _albumSort.value = order }

    fun sync() {
        // compareAndSet avoids a TOCTOU race where two rapid triggers (pull-to-refresh
        // + onResume, for example) both pass the guard before either flips it.
        if (!_isSyncing.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            try {
                runCatching { repo.sync() }
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
