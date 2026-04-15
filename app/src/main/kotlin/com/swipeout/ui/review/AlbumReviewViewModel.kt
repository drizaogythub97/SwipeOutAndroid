package com.swipeout.ui.review

import android.app.Activity
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swipeout.data.db.entity.ImageEntity
import com.swipeout.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Review ViewModel for the album-based swipe flow.
 *
 * Key difference from [ReviewViewModel]: confirming deletion here does NOT call
 * markMonthCompleted. Months become complete automatically when their pending count
 * reaches zero — which happens naturally after sync() rebuilds the menus.
 */
@HiltViewModel
class AlbumReviewViewModel @Inject constructor(
    private val repo: MediaRepository,
    savedState: SavedStateHandle,
) : ViewModel() {

    val bucketId: Long   = checkNotNull(savedState["bucketId"])
    val albumName: String = savedState.get<String>("albumName") ?: ""

    private val _state = MutableStateFlow(ReviewUiState(isLoading = true))
    val state: StateFlow<ReviewUiState> get() = _state

    init { loadImages() }

    private fun loadImages() {
        viewModelScope.launch {
            val kept       = repo.getKeptImagesByAlbum(bucketId)
            val deleted    = repo.getDeletedImagesByAlbum(bucketId)
            val bookmarked = repo.getBookmarkedImagesByAlbum(bucketId)
            _state.value = ReviewUiState(
                keptImages       = kept,
                deletedImages    = deleted,
                bookmarkedImages = bookmarked,
                deletedBytes     = deleted.sumOf { it.sizeBytes },
                isLoading        = false,
            )
        }
    }

    fun revert(image: ImageEntity) {
        viewModelScope.launch {
            val newDecision = if (image.decision == ImageEntity.DELETE) ImageEntity.KEEP else ImageEntity.DELETE
            repo.swipe(image.id, newDecision)
            loadImages()
        }
    }

    fun confirmReview(activity: Activity) {
        if (_state.value.deletedImages.isNotEmpty()) {
            confirmDelete(activity)
        } else {
            // Nothing to delete — just finish the album session
            _state.value = _state.value.copy(isDone = true)
        }
    }

    private fun confirmDelete(activity: Activity) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val sender = repo.buildDeleteRequestForAlbum(activity, bucketId)
                if (sender != null) {
                    _state.value = _state.value.copy(isLoading = false, pendingDeleteSender = sender)
                    return@launch
                }
            }
            repo.deleteAlbumDirectly(activity, bucketId)
            _state.value = _state.value.copy(isLoading = false, isDone = true)
        }
    }

    fun onSystemDeleteConfirmed() {
        viewModelScope.launch {
            repo.onAlbumDeleteConfirmed(bucketId)
            _state.value = _state.value.copy(isDone = true, pendingDeleteSender = null)
        }
    }

    fun clearPendingSender() {
        _state.value = _state.value.copy(pendingDeleteSender = null, isLoading = false)
    }
}
