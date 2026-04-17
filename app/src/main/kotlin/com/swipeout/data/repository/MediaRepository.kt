package com.swipeout.data.repository

import android.app.Activity
import android.content.ContentUris
import android.content.IntentSender
import android.os.Build
import android.provider.MediaStore
import com.swipeout.data.db.dao.DeletionEventDao
import com.swipeout.data.db.dao.ImageDao
import com.swipeout.data.db.dao.MonthlyMenuDao
import com.swipeout.data.db.entity.DeletionEventEntity
import com.swipeout.data.db.entity.ImageEntity
import com.swipeout.data.db.entity.MonthlyMenuEntity
import com.swipeout.data.media.MediaStoreSync
import com.swipeout.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of a deletion attempt. Failures can occur when the OS blocks the
 * physical delete (file locked, cloud-only, permission revoked). The UI uses
 * [hasFailures] to surface a message so users know the items are back in the
 * review queue instead of silently gone.
 */
data class DeleteResult(
    val succeeded: Int,
    val failed: Int,
    val bytesFreed: Long,
) {
    val hasFailures: Boolean get() = failed > 0
    companion object { val EMPTY = DeleteResult(0, 0, 0L) }
}

@Singleton
class MediaRepository @Inject constructor(
    private val imageDao: ImageDao,
    private val monthlyMenuDao: MonthlyMenuDao,
    private val deletionEventDao: DeletionEventDao,
    private val mediaStoreSync: MediaStoreSync,
    private val userPrefs: UserPreferencesRepository,
) {
    val months: Flow<List<MonthlyMenuEntity>> = monthlyMenuDao.getAllMenus()

    suspend fun sync() = mediaStoreSync.sync()

    suspend fun refreshMenus() = mediaStoreSync.refreshMenus()

    fun getPendingImages(monthKey: String): Flow<List<ImageEntity>> =
        imageDao.getPendingImages(monthKey)

    fun getTotalCount(monthKey: String): Flow<Int> =
        imageDao.getTotalCount(monthKey)

    fun getAlbums(): Flow<List<ImageDao.AlbumInfo>> =
        imageDao.getAlbumsWithPending()

    // ── Album-based swipe/review ──────────────────────────────────────────────

    fun getPendingImagesByAlbum(bucketId: Long): Flow<List<ImageEntity>> =
        imageDao.getPendingImagesByAlbum(bucketId)

    fun getTotalCountByAlbum(bucketId: Long): Flow<Int> =
        imageDao.getTotalCountByAlbum(bucketId)

    suspend fun getDeletedImagesByAlbum(bucketId: Long)    = imageDao.getByDecisionAndAlbum(bucketId, ImageEntity.DELETE)
    suspend fun getKeptImagesByAlbum(bucketId: Long)       = imageDao.getByDecisionAndAlbum(bucketId, ImageEntity.KEEP)
    suspend fun getBookmarkedImagesByAlbum(bucketId: Long) = imageDao.getByDecisionAndAlbum(bucketId, ImageEntity.BOOKMARK)

    suspend fun buildDeleteRequestForAlbum(activity: Activity, bucketId: Long): IntentSender? =
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return@withContext null
            val items = imageDao.getByDecisionAndAlbum(bucketId, ImageEntity.DELETE)
            if (items.isEmpty()) return@withContext null
            val uris = items.map { it.toContentUri() }
            MediaStore.createDeleteRequest(activity.contentResolver, uris).intentSender
        }

    /**
     * Confirms deletion for an album review session (API 30+ system dialog path).
     * Sync reconciles Room with MediaStore; anything still present = failed delete
     * and gets reset to PENDING so it reappears in the album's pending list.
     */
    suspend fun onAlbumDeleteConfirmed(bucketId: Long): DeleteResult = withContext(Dispatchers.IO) {
        val items = imageDao.getByDecisionAndAlbum(bucketId, ImageEntity.DELETE)
        if (items.isEmpty()) return@withContext DeleteResult.EMPTY
        reconcileAfterSystemDelete(items)
    }

    /**
     * Direct-delete path (API < 30, or when an app owns the files outright).
     * [contentResolver.delete] returns the row count; 0 means the row is still
     * there, so we flip those back to PENDING.
     */
    suspend fun deleteAlbumDirectly(activity: Activity, bucketId: Long): DeleteResult = withContext(Dispatchers.IO) {
        val items = imageDao.getByDecisionAndAlbum(bucketId, ImageEntity.DELETE)
        if (items.isEmpty()) return@withContext DeleteResult.EMPTY
        reconcileAfterDirectDelete(activity, items, monthToComplete = null)
    }

    suspend fun swipe(imageId: Long, decision: String) = withContext(Dispatchers.IO) {
        imageDao.updateDecision(imageId, decision)
    }

    suspend fun markMonthCompleted(monthKey: String) = withContext(Dispatchers.IO) {
        monthlyMenuDao.markCompleted(monthKey)
    }

    suspend fun getDeletedImages(monthKey: String)    = imageDao.getByDecision(monthKey, ImageEntity.DELETE)
    suspend fun getKeptImages(monthKey: String)       = imageDao.getByDecision(monthKey, ImageEntity.KEEP)
    suspend fun getBookmarkedImages(monthKey: String) = imageDao.getByDecision(monthKey, ImageEntity.BOOKMARK)

    suspend fun buildDeleteRequest(activity: Activity, monthKey: String): IntentSender? =
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return@withContext null
            val items = imageDao.getByDecision(monthKey, ImageEntity.DELETE)
            if (items.isEmpty()) return@withContext null
            val uris = items.map { it.toContentUri() }
            MediaStore.createDeleteRequest(activity.contentResolver, uris).intentSender
        }

    suspend fun onDeleteConfirmed(monthKey: String): DeleteResult = withContext(Dispatchers.IO) {
        val items = imageDao.getByDecision(monthKey, ImageEntity.DELETE)
        if (items.isEmpty()) return@withContext DeleteResult.EMPTY
        reconcileAfterSystemDelete(items, monthToComplete = monthKey)
    }

    suspend fun deleteDirectly(activity: Activity, monthKey: String): DeleteResult = withContext(Dispatchers.IO) {
        val items = imageDao.getByDecision(monthKey, ImageEntity.DELETE)
        if (items.isEmpty()) return@withContext DeleteResult.EMPTY
        reconcileAfterDirectDelete(activity, items, monthToComplete = monthKey)
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    /**
     * API 30+ path: the system dialog already performed the deletes. We sync
     * Room with MediaStore, then anything among our target IDs still present
     * is treated as a failure and rolled back to PENDING.
     */
    private suspend fun reconcileAfterSystemDelete(
        items: List<ImageEntity>,
        monthToComplete: String? = null,
    ): DeleteResult {
        mediaStoreSync.sync()

        val targetIds = items.map { it.id }
        val stillPresent = imageDao.getExistingIds(targetIds).toHashSet()
        val succeededItems = items.filter { it.id !in stillPresent }
        val failedItems    = items.filter { it.id in stillPresent }
        val bytesFreed     = succeededItems.sumOf { it.sizeBytes }

        if (succeededItems.isNotEmpty()) {
            userPrefs.addBytesFreed(bytesFreed)
            deletionEventDao.insert(DeletionEventEntity(
                timestampMs = System.currentTimeMillis(),
                fileCount   = succeededItems.size,
                bytesFreed  = bytesFreed,
            ))
        }
        for (item in failedItems) imageDao.updateDecision(item.id, ImageEntity.PENDING)

        if (failedItems.isEmpty() && monthToComplete != null) {
            monthlyMenuDao.markCompleted(monthToComplete)
        }
        // Refresh aggregates so Home reflects the post-delete state.
        mediaStoreSync.refreshMenus()
        return DeleteResult(succeededItems.size, failedItems.size, bytesFreed)
    }

    /**
     * Pre-API-30 path: we call [contentResolver.delete] ourselves and can trust
     * its return value (0 = row still there = failure). Only the successful
     * rows are removed from Room; failures flip back to PENDING.
     */
    private suspend fun reconcileAfterDirectDelete(
        activity: Activity,
        items: List<ImageEntity>,
        monthToComplete: String?,
    ): DeleteResult {
        val succeeded = mutableListOf<ImageEntity>()
        val failed    = mutableListOf<ImageEntity>()

        for (item in items) {
            val removed = runCatching {
                activity.contentResolver.delete(item.toContentUri(), null, null)
            }.getOrDefault(0)
            if (removed > 0) succeeded += item else failed += item
        }

        val bytesFreed = succeeded.sumOf { it.sizeBytes }
        if (succeeded.isNotEmpty()) {
            imageDao.deleteByIds(succeeded.map { it.id })
            userPrefs.addBytesFreed(bytesFreed)
            deletionEventDao.insert(DeletionEventEntity(
                timestampMs = System.currentTimeMillis(),
                fileCount   = succeeded.size,
                bytesFreed  = bytesFreed,
            ))
        }
        for (item in failed) imageDao.updateDecision(item.id, ImageEntity.PENDING)

        if (failed.isEmpty() && monthToComplete != null) {
            monthlyMenuDao.markCompleted(monthToComplete)
        }
        mediaStoreSync.sync()
        return DeleteResult(succeeded.size, failed.size, bytesFreed)
    }

    private fun ImageEntity.toContentUri() =
        if (mimeType.startsWith("video/"))
            ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
        else
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
}
