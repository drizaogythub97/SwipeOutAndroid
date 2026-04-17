package com.swipeout.ui.swipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swipeout.data.db.entity.ImageEntity
import com.swipeout.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LastSwipe(
    val image: ImageEntity,
    val fromRight: Boolean, // true = swiped right (KEEP) → card returns from right on undo
)

@HiltViewModel
class SwipeViewModel @Inject constructor(
    private val repo: MediaRepository,
    savedState: SavedStateHandle,
) : ViewModel() {

    // Either monthKey (month mode) or bucketId (album mode) is present — never both.
    val monthKey: String? = savedState.get<String>("monthKey")
    val bucketId: Long?   = savedState.get<Long>("bucketId")

    val isAlbumMode: Boolean get() = bucketId != null

    // null = Room hasn't responded yet (distinct from "loaded and empty").
    // This prevents isAllReviewed from firing before the first real DB emission.
    val pendingImages: StateFlow<List<ImageEntity>?> = when {
        monthKey != null -> repo.getPendingImages(monthKey)
        bucketId != null -> repo.getPendingImagesByAlbum(bucketId)
        else             -> flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Total declared before isAllReviewed — used in combine()
    val totalCount: StateFlow<Int> = when {
        monthKey != null -> repo.getTotalCount(monthKey)
        bucketId != null -> repo.getTotalCountByAlbum(bucketId)
        else             -> flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // True only when: Room has responded (pending != null) AND month/album has images AND all are reviewed.
    val isAllReviewed: StateFlow<Boolean> = combine(pendingImages, totalCount) { pending, total ->
        pending != null && total > 0 && pending.isEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // ── Undo ─────────────────────────────────────────────────────────────────

    private val _swipeHistory = MutableStateFlow<List<LastSwipe>>(emptyList())

    val lastSwipe: StateFlow<LastSwipe?> = _swipeHistory
        .map { it.lastOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val canUndo: StateFlow<Boolean> = _swipeHistory
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /**
     * Records the swipe and updates Room. Takes the full [ImageEntity] (not just an id)
     * because the UI already holds the entity — looking it up from [pendingImages] races
     * with Room emissions and occasionally returned null under rapid swiping.
     */
    fun swipe(image: ImageEntity, decision: String) {
        _swipeHistory.update { history ->
            val next = history + LastSwipe(
                image     = image,
                fromRight = decision == ImageEntity.KEEP,
            )
            // Cap undo history — long review sessions would otherwise retain every card
            // in memory. 50 is far beyond any realistic undo chain.
            if (next.size > HISTORY_LIMIT) next.takeLast(HISTORY_LIMIT) else next
        }
        viewModelScope.launch {
            repo.swipe(image.id, decision)
            repo.refreshMenus()
        }
    }

    fun undo() {
        val history = _swipeHistory.value
        if (history.isEmpty()) return
        val last = history.last()
        _swipeHistory.update { it.dropLast(1) }
        viewModelScope.launch {
            repo.swipe(last.image.id, ImageEntity.PENDING)
            repo.refreshMenus()
        }
    }

    private companion object {
        const val HISTORY_LIMIT = 50
    }
}
