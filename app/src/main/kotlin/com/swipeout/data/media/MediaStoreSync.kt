package com.swipeout.data.media

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.swipeout.data.db.dao.ImageDao
import com.swipeout.data.db.dao.MonthlyMenuDao
import com.swipeout.data.db.entity.ImageEntity
import com.swipeout.data.db.entity.MonthlyMenuEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreSync @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageDao: ImageDao,
    private val monthlyMenuDao: MonthlyMenuDao,
) {
    // Single source of truth for concurrent syncs — delete flows + pull-to-refresh
    // can race otherwise.
    private val mutex = Mutex()

    suspend fun sync() = mutex.withLock {
        withContext(Dispatchers.IO) { syncInternal() }
    }

    private suspend fun syncInternal() {
        // 1) Fast pass: fetch all live MediaStore IDs (tiny projection)
        val liveIds = collectLiveIds()

        // 2) Reconcile deletions: anything in Room but missing in MediaStore is gone
        val existingIds = imageDao.getAllIds().toHashSet()
        val removedIds = existingIds - liveIds
        if (removedIds.isNotEmpty()) {
            removedIds.chunked(900).forEach { imageDao.deleteByIds(it) }
        }

        // 3) Incremental insert: fetch full columns only for IDs not yet in Room
        val newIds = liveIds - existingIds
        if (newIds.isNotEmpty()) {
            val newItems = fetchDetailsForIds(newIds)
            if (newItems.isNotEmpty()) imageDao.insertNew(newItems)
        }

        // 4) Backfill bucket info once (cheap existence check gates the scan)
        if (imageDao.hasMissingBucketInfo()) backfillBucketInfo()

        // 5) Rebuild menu aggregates
        rebuildMenus()
    }

    /** Returns every image/video id currently visible in MediaStore (excluding trashed/pending). */
    private fun collectLiveIds(): HashSet<Long> {
        val ids = HashSet<Long>()
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = buildSelection(includeDate = false)
        context.contentResolver.query(
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
            projection, selection, null, null,
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            while (cursor.moveToNext()) ids += cursor.getLong(idCol)
        }
        return ids
    }

    private fun fetchDetailsForIds(newIds: Set<Long>): List<ImageEntity> {
        val out = mutableListOf<ImageEntity>()
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            "bucket_id",
            "bucket_display_name",
        )

        // SQLite bind-arg limit is ~999; batch IDs so the IN () clause stays safe.
        newIds.chunked(500).forEach { batch ->
            val placeholders = batch.joinToString(",") { "?" }
            val baseSel = buildSelection(includeDate = false)
            val selection = "$baseSel AND ${MediaStore.MediaColumns._ID} IN ($placeholders)"
            val args = batch.map { it.toString() }.toTypedArray()

            context.contentResolver.query(
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                projection, selection, args, null,
            )?.use { cursor ->
                val idCol         = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val dateCol       = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
                val sizeCol       = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val mimeCol       = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val durationCol   = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)
                val widthCol      = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)
                val heightCol     = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)
                val bucketIdCol   = cursor.getColumnIndex("bucket_id")
                val bucketNameCol = cursor.getColumnIndex("bucket_display_name")

                while (cursor.moveToNext()) {
                    val id       = cursor.getLong(idCol)
                    val mimeType = cursor.getString(mimeCol) ?: continue

                    val dateAdded = cursor.getLong(dateCol)
                    val cal = Calendar.getInstance().apply { timeInMillis = dateAdded * 1000L }
                    val monthKey = "%04d-%02d".format(
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1
                    )
                    val baseUri = if (mimeType.startsWith("video/"))
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    else
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                    out += ImageEntity(
                        id         = id,
                        contentUri = ContentUris.withAppendedId(baseUri, id).toString(),
                        monthKey   = monthKey,
                        dateAdded  = dateAdded,
                        sizeBytes  = cursor.getLong(sizeCol),
                        mimeType   = mimeType,
                        durationMs = cursor.getLong(durationCol),
                        width      = cursor.getInt(widthCol),
                        height     = cursor.getInt(heightCol),
                        bucketId   = if (bucketIdCol   >= 0) cursor.getLong(bucketIdCol)           else 0L,
                        bucketName = if (bucketNameCol >= 0) cursor.getString(bucketNameCol) ?: "" else "",
                    )
                }
            }
        }
        return out
    }

    /**
     * MIME filter + excludes trashed/pending on Android 11+.
     * IS_TRASHED/IS_PENDING columns exist in API 30+; older APIs use the MIME filter alone.
     */
    private fun buildSelection(includeDate: Boolean): String {
        val mime = "(${MediaStore.MediaColumns.MIME_TYPE} LIKE 'image/%' " +
                "OR ${MediaStore.MediaColumns.MIME_TYPE} LIKE 'video/%')"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            "$mime AND ${MediaStore.MediaColumns.IS_TRASHED} = 0 " +
                    "AND ${MediaStore.MediaColumns.IS_PENDING} = 0"
        } else mime
    }

    /**
     * One-shot fill for records that pre-date the album migration. Guarded by
     * [ImageDao.hasMissingBucketInfo] — after the first post-upgrade sync it's a no-op.
     */
    private suspend fun backfillBucketInfo() {
        val bucketMap = mutableMapOf<Long, Pair<Long, String>>()
        context.contentResolver.query(
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
            arrayOf(MediaStore.MediaColumns._ID, "bucket_id", "bucket_display_name"),
            buildSelection(includeDate = false),
            null,
            null,
        )?.use { cursor ->
            val idCol     = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val bktIdCol  = cursor.getColumnIndex("bucket_id")
            val bktNmCol  = cursor.getColumnIndex("bucket_display_name")
            while (cursor.moveToNext()) {
                val id         = cursor.getLong(idCol)
                val bucketId   = if (bktIdCol >= 0) cursor.getLong(bktIdCol)           else 0L
                val bucketName = if (bktNmCol >= 0) cursor.getString(bktNmCol) ?: "" else ""
                if (bucketId != 0L) bucketMap[id] = Pair(bucketId, bucketName)
            }
        }

        for ((id, pair) in bucketMap) {
            imageDao.updateBucketInfo(id, pair.first, pair.second)
        }
    }

    /**
     * Rebuilds menu aggregates. Preserves any existing `is_completed = true` flag so
     * months the user has already finished reviewing don't lose their checkmark when
     * every image has been physically deleted.
     */
    private suspend fun rebuildMenus() {
        val summaries = imageDao.getMonthSummaries()
        val menus = summaries.map { s ->
            val existing = monthlyMenuDao.getMenu(s.monthKey)
            val wasCompleted = existing?.isCompleted ?: false
            MonthlyMenuEntity(
                key            = s.monthKey,
                title          = buildTitle(s.monthKey),
                totalCount     = s.total,
                pendingCount   = s.pending,
                keptCount      = s.kept,
                deletedCount   = s.deleted,
                bookmarkedCount= s.bookmarked,
                isCompleted    = wasCompleted || (s.pending == 0 && s.total > 0),
                coverUri       = imageDao.getCoverUri(s.monthKey) ?: "",
            )
        }
        monthlyMenuDao.upsertAll(menus)
        monthlyMenuDao.removeStale(menus.map { it.key })
    }

    private fun buildTitle(monthKey: String): String {
        val parts = monthKey.split("-")
        if (parts.size != 2) return monthKey
        val year = parts[0].toIntOrNull() ?: return monthKey
        val month = parts[1].toIntOrNull() ?: return monthKey
        val monthNames = listOf(
            "Janeiro","Fevereiro","Março","Abril","Maio","Junho",
            "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"
        )
        return "${monthNames.getOrElse(month - 1) { monthKey }} $year"
    }

    /**
     * Exposed so ViewModels can trigger a pure menu rebuild (e.g., after undo)
     * without paying the full MediaStore scan.
     */
    suspend fun refreshMenus() = mutex.withLock {
        withContext(Dispatchers.IO) { rebuildMenus() }
    }
}
