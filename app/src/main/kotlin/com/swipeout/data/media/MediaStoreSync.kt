package com.swipeout.data.media

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.swipeout.data.db.dao.ImageDao
import com.swipeout.data.db.dao.MonthlyMenuDao
import com.swipeout.data.db.entity.ImageEntity
import com.swipeout.data.db.entity.MonthlyMenuEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
    suspend fun sync() = withContext(Dispatchers.IO) {
        val existingIds = imageDao.getAllIds().toHashSet()
        val newItems = mutableListOf<ImageEntity>()

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
        val selection =
            "(${MediaStore.MediaColumns.MIME_TYPE} LIKE 'image/%' OR ${MediaStore.MediaColumns.MIME_TYPE} LIKE 'video/%')"

        context.contentResolver.query(
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
            projection, selection, null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC",
        )?.use { cursor ->
            val idCol         = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val dateCol       = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val sizeCol       = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val mimeCol       = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val durationCol   = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)
            val widthCol      = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)
            val heightCol     = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)
            // bucket columns may not be available on all devices/API levels — use safe access
            val bucketIdCol   = cursor.getColumnIndex("bucket_id")
            val bucketNameCol = cursor.getColumnIndex("bucket_display_name")

            while (cursor.moveToNext()) {
                val id       = cursor.getLong(idCol)
                val mimeType = cursor.getString(mimeCol) ?: continue
                if (id in existingIds) continue      // preserve existing decisions

                val dateAdded = cursor.getLong(dateCol)
                val cal = Calendar.getInstance().apply { timeInMillis = dateAdded * 1000L }
                val monthKey = "%04d-%02d".format(
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1
                )
                val baseUri = if (mimeType.startsWith("video/"))
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                else
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                newItems += ImageEntity(
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

        if (newItems.isNotEmpty()) imageDao.insertNew(newItems)

        // Backfill bucket info for records synced before migration 2→3.
        // The check is a single indexed COUNT — effectively free.
        // After first sync post-upgrade, all records have bucket_id > 0 and this block is never entered again.
        backfillBucketInfo()

        rebuildMenus()
    }

    /**
     * Fills bucket_id/bucket_name for records that pre-date the album migration.
     * Runs a lightweight MediaStore query (3 columns only) and batch-updates in bulk.
     * Skipped entirely on subsequent syncs once all records are up-to-date.
     */
    private suspend fun backfillBucketInfo() {
        val idsMissing = imageDao.getIdsMissingBucketInfo()
        if (idsMissing.isEmpty()) return

        val bucketMap = mutableMapOf<Long, Pair<Long, String>>()
        context.contentResolver.query(
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
            arrayOf(MediaStore.MediaColumns._ID, "bucket_id", "bucket_display_name"),
            "(${MediaStore.MediaColumns.MIME_TYPE} LIKE 'image/%' OR ${MediaStore.MediaColumns.MIME_TYPE} LIKE 'video/%')",
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

        val missingSet = idsMissing.toHashSet()
        for ((id, pair) in bucketMap) {
            if (id in missingSet) {
                imageDao.updateBucketInfo(id, pair.first, pair.second)
            }
        }
    }

    private suspend fun rebuildMenus() {
        val summaries = imageDao.getMonthSummaries()
        val menus = summaries.map { s ->
            MonthlyMenuEntity(
                key            = s.monthKey,
                title          = buildTitle(s.monthKey),
                totalCount     = s.total,
                pendingCount   = s.pending,
                keptCount      = s.kept,
                deletedCount   = s.deleted,
                bookmarkedCount= s.bookmarked,
                isCompleted    = s.pending == 0 && s.total > 0,
                coverUri       = imageDao.getCoverUri(s.monthKey) ?: "",
            )
        }
        monthlyMenuDao.upsertAll(menus)
        if (menus.isNotEmpty()) {
            monthlyMenuDao.removeStale(menus.map { it.key })
        }
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
}
