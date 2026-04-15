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
            val uris = items.map { item ->
                if (item.mimeType.startsWith("video/"))
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, item.id)
                else
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, item.id)
            }
            MediaStore.createDeleteRequest(activity.contentResolver, uris).intentSender
        }

    /**
     * Confirms deletion for an album review session.
     * Unlike [onDeleteConfirmed], does NOT call [markMonthCompleted] — months become
     * complete automatically via rebuildMenus() when their pending count reaches zero.
     */
    suspend fun onAlbumDeleteConfirmed(bucketId: Long) = withContext(Dispatchers.IO) {
        val items = imageDao.getByDecisionAndAlbum(bucketId, ImageEntity.DELETE)
        val bytesFreed = items.sumOf { it.sizeBytes }
        imageDao.deleteByIds(items.map { it.id })
        userPrefs.addBytesFreed(bytesFreed)
        deletionEventDao.insert(DeletionEventEntity(
            timestampMs = System.currentTimeMillis(),
            fileCount   = items.size,
            bytesFreed  = bytesFreed,
        ))
        mediaStoreSync.sync()
    }

    suspend fun deleteAlbumDirectly(activity: Activity, bucketId: Long) = withContext(Dispatchers.IO) {
        val items = imageDao.getByDecisionAndAlbum(bucketId, ImageEntity.DELETE)
        val bytesFreed = items.sumOf { it.sizeBytes }
        for (item in items) {
            runCatching {
                val uri = if (item.mimeType.startsWith("video/"))
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, item.id)
                else
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, item.id)
                activity.contentResolver.delete(uri, null, null)
            }
        }
        imageDao.deleteByIds(items.map { it.id })
        userPrefs.addBytesFreed(bytesFreed)
        deletionEventDao.insert(DeletionEventEntity(
            timestampMs = System.currentTimeMillis(),
            fileCount   = items.size,
            bytesFreed  = bytesFreed,
        ))
        mediaStoreSync.sync()
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
            val uris = items.map { item ->
                if (item.mimeType.startsWith("video/"))
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, item.id)
                else
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, item.id)
            }
            MediaStore.createDeleteRequest(activity.contentResolver, uris).intentSender
        }

    suspend fun onDeleteConfirmed(monthKey: String) = withContext(Dispatchers.IO) {
        val items = imageDao.getByDecision(monthKey, ImageEntity.DELETE)
        val bytesFreed = items.sumOf { it.sizeBytes }
        imageDao.deleteByIds(items.map { it.id })
        monthlyMenuDao.markCompleted(monthKey)
        userPrefs.addBytesFreed(bytesFreed)
        deletionEventDao.insert(
            DeletionEventEntity(
                timestampMs = System.currentTimeMillis(),
                fileCount   = items.size,
                bytesFreed  = bytesFreed,
            )
        )
        mediaStoreSync.sync()
    }

    suspend fun deleteDirectly(activity: Activity, monthKey: String) = withContext(Dispatchers.IO) {
        val items = imageDao.getByDecision(monthKey, ImageEntity.DELETE)
        val bytesFreed = items.sumOf { it.sizeBytes }
        for (item in items) {
            runCatching {
                val uri = if (item.mimeType.startsWith("video/"))
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, item.id)
                else
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, item.id)
                activity.contentResolver.delete(uri, null, null)
            }
        }
        imageDao.deleteByIds(items.map { it.id })
        monthlyMenuDao.markCompleted(monthKey)
        userPrefs.addBytesFreed(bytesFreed)
        deletionEventDao.insert(
            DeletionEventEntity(
                timestampMs = System.currentTimeMillis(),
                fileCount   = items.size,
                bytesFreed  = bytesFreed,
            )
        )
        mediaStoreSync.sync()
    }
}
