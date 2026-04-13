package com.swipeout.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swipeout.data.db.dao.ImageDao
import com.swipeout.data.db.entity.MonthlyMenuEntity
import com.swipeout.data.preferences.UserPreferencesRepository
import com.swipeout.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: MediaRepository,
    userPrefs: UserPreferencesRepository,
) : ViewModel() {

    private val allMonths: StateFlow<List<MonthlyMenuEntity>> = repo.months
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalBytesFreed: StateFlow<Long> = userPrefs.totalBytesFreed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    // ── Album filter ──────────────────────────────────────────────────────────

    /** Albums that still have pending images — only subscribed when HomeScreen is visible. */
    val albums: StateFlow<List<ImageDao.AlbumInfo>> = repo.getAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Currently selected album bucket_id. null = "Todos" (no filter). */
    val selectedBucketId = MutableStateFlow<Long?>(null)

    /**
     * Month keys that belong to the selected album.
     * null = no filter active (default path — zero overhead).
     * Switches automatically via flatMapLatest when selectedBucketId changes.
     */
    private val filteredMonthKeys: StateFlow<Set<String>?> = selectedBucketId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repo.getMonthKeysForAlbum(id).map { it.toHashSet() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * The month list shown to the user.
     * - No filter selected: identical to allMonths — no extra processing.
     * - Filter selected: in-memory filter using the pre-computed monthKeys set.
     */
    val months: StateFlow<List<MonthlyMenuEntity>> = combine(
        allMonths, filteredMonthKeys
    ) { all, monthKeys ->
        if (monthKeys == null) all else all.filter { it.key in monthKeys }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectAlbum(bucketId: Long?) { selectedBucketId.value = bucketId }

    // ── Sync ─────────────────────────────────────────────────────────────────

    fun sync() {
        if (_isSyncing.value) return   // guard against concurrent syncs
        viewModelScope.launch {
            _isSyncing.value = true
            runCatching { repo.sync() }
            _isSyncing.value = false
        }
    }
}
