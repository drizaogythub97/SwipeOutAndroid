package com.swipeout.ui.review

import android.app.Activity
import android.content.IntentSender
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swipeout.data.db.entity.ImageEntity
import com.swipeout.data.repository.DeleteResult
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
    // Non-zero when the OS refused to delete some files — UI surfaces it as a Toast.
    val failedDeleteCount: Int              = 0,
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
            // DELETE↔KEEP toggle preserves the original Tinder-style review UX.
            // BOOKMARK flips to PENDING so the user can re-review — it has no natural opposite.
            val newDecision = when (image.decision) {
                ImageEntity.DELETE   -> ImageEntity.KEEP
                ImageEntity.KEEP     -> ImageEntity.DELETE
                ImageEntity.BOOKMARK -> ImageEntity.PENDING
                else                 -> return@launch
            }
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
            val result = repo.deleteDirectly(activity, monthKey)
            finishWithResult(result)
        }
    }

    fun onSystemDeleteConfirmed() {
        viewModelScope.launch {
            val result = repo.onDeleteConfirmed(monthKey)
            finishWithResult(result)
        }
    }

    private fun finishWithResult(result: DeleteResult) {
        _state.value = _state.value.copy(
            isLoading          = false,
            isDone             = true,
            pendingDeleteSender = null,
            failedDeleteCount  = result.failed,
        )
    }

    fun clearPendingSender() {
        _state.value = _state.value.copy(pendingDeleteSender = null, isLoading = false)
    }
}
