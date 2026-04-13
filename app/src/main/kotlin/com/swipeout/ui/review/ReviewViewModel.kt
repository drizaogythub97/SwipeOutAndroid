package com.swipeout.ui.review

import android.app.Activity
import android.content.IntentSender
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

data class ReviewUiState(
    val keptImages: List<ImageEntity>       = emptyList(),
    val deletedImages: List<ImageEntity>    = emptyList(),
    val bookmarkedImages: List<ImageEntity> = emptyList(),
    val deletedBytes: Long                  = 0L,
    val isLoading: Boolean                  = false,
    val isDone: Boolean                     = false,
    val pendingDeleteSender: IntentSender?  = null,
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repo: MediaRepository,
    savedState: SavedStateHandle,
) : ViewModel() {

    val monthKey: String = checkNotNull(savedState["monthKey"])

    private val _state = MutableStateFlow(ReviewUiState(isLoading = true))
    val state: StateFlow<ReviewUiState> get() = _state

    init { loadImages() }

    private fun loadImages() {
        viewModelScope.launch {
            val kept       = repo.getKeptImages(monthKey)
            val deleted    = repo.getDeletedImages(monthKey)
            val bookmarked = repo.getBookmarkedImages(monthKey)
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

    /**
     * Confirma a revisão do mês.
     * - Se houver itens para deletar: dispara o fluxo de deleção do sistema.
     * - Se não houver: apenas marca o mês como concluído.
     */
    fun confirmReview(activity: Activity) {
        val hasItemsToDelete = _state.value.deletedImages.isNotEmpty()
        if (hasItemsToDelete) {
            confirmDelete(activity)
        } else {
            viewModelScope.launch {
                _state.value = _state.value.copy(isLoading = true)
                repo.markMonthCompleted(monthKey)
                _state.value = _state.value.copy(isLoading = false, isDone = true)
            }
        }
    }

    private fun confirmDelete(activity: Activity) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val sender = repo.buildDeleteRequest(activity, monthKey)
                if (sender != null) {
                    _state.value = _state.value.copy(isLoading = false, pendingDeleteSender = sender)
                    return@launch
                }
            }
            repo.deleteDirectly(activity, monthKey)
            _state.value = _state.value.copy(isLoading = false, isDone = true)
        }
    }

    fun onSystemDeleteConfirmed() {
        viewModelScope.launch {
            repo.onDeleteConfirmed(monthKey)
            _state.value = _state.value.copy(isDone = true, pendingDeleteSender = null)
        }
    }

    fun clearPendingSender() {
        _state.value = _state.value.copy(pendingDeleteSender = null, isLoading = false)
    }
}
