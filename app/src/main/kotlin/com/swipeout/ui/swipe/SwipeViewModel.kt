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

    val monthKey: String = checkNotNull(savedState["monthKey"])

    // null = Room hasn't responded yet (distinct from "loaded and empty").
    // This prevents isAllReviewed from firing before the first real DB emission.
    val pendingImages: StateFlow<List<ImageEntity>?> =
        repo.getPendingImages(monthKey)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Total declared before isAllReviewed — used in combine()
    val totalCount: StateFlow<Int> =
        repo.getTotalCount(monthKey)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // True only when: Room has responded (pending != null) AND month has images AND all are reviewed.
    val isAllReviewed: StateFlow<Boolean> = combine(pendingImages, totalCount) { pending, total ->
        pending != null && total > 0 && pending.isEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // ── Undo ─────────────────────────────────────────────────────────────────
    // Full history stack — supports unlimited undo back to the first card.

    private val _swipeHistory = MutableStateFlow<List<LastSwipe>>(emptyList())

    // Exposes the most recent swipe (top of stack) for returning-card animation.
    val lastSwipe: StateFlow<LastSwipe?> = _swipeHistory
        .map { it.lastOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val canUndo: StateFlow<Boolean> = _swipeHistory
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun swipe(imageId: Long, decision: String) {
        // Capture image synchronously before it leaves pendingImages
        val image = pendingImages.value?.firstOrNull { it.id == imageId }
        if (image != null) {
            _swipeHistory.update { history ->
                history + LastSwipe(
                    image     = image,
                    fromRight = decision == ImageEntity.KEEP,
                )
            }
        }
        viewModelScope.launch { repo.swipe(imageId, decision) }
    }

    fun undo() {
        val history = _swipeHistory.value
        if (history.isEmpty()) return
        val last = history.last()
        _swipeHistory.update { it.dropLast(1) }
        viewModelScope.launch { repo.swipe(last.image.id, ImageEntity.PENDING) }
    }
}
