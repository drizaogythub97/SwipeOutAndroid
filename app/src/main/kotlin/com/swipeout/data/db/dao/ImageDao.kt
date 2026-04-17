package com.swipeout.data.db.dao

import androidx.room.*
import com.swipeout.data.db.entity.ImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {

    @Query("SELECT * FROM images WHERE month_key = :monthKey AND decision = 'PENDING' ORDER BY date_added ASC")
    fun getPendingImages(monthKey: String): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE month_key = :monthKey AND decision = :decision ORDER BY date_added ASC")
    suspend fun getByDecision(monthKey: String, decision: String): List<ImageEntity>

    @Query("SELECT id FROM images")
    suspend fun getAllIds(): List<Long>

    @Query("SELECT id FROM images WHERE month_key = :monthKey")
    suspend fun getIdsInMonth(monthKey: String): List<Long>

    /** Returns the subset of [ids] that still exist in the table — used to detect failed deletions. */
    @Query("SELECT id FROM images WHERE id IN (:ids)")
    suspend fun getExistingIds(ids: List<Long>): List<Long>

    @Query("SELECT MAX(date_added) FROM images")
    suspend fun getMaxDateAdded(): Long?

    /** Cheap existence check for the legacy backfill branch. */
    @Query("SELECT EXISTS(SELECT 1 FROM images WHERE bucket_id = 0)")
    suspend fun hasMissingBucketInfo(): Boolean

    // Prefers image files over videos for the cover — videos can't be thumbnailed by Coil directly
    @Query("""
        SELECT content_uri FROM images
        WHERE month_key = :monthKey
        ORDER BY CASE WHEN mime_type LIKE 'image/%' THEN 0 ELSE 1 END, date_added DESC
        LIMIT 1
    """)
    suspend fun getCoverUri(monthKey: String): String?

    @Query("SELECT COUNT(*) FROM images WHERE month_key = :monthKey")
    fun getTotalCount(monthKey: String): Flow<Int>

    @Query("UPDATE images SET decision = :decision WHERE id = :id")
    suspend fun updateDecision(id: Long, decision: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNew(images: List<ImageEntity>)

    @Query("DELETE FROM images WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    // ── Album filter ─────────────────────────────────────────────────────────

    data class AlbumInfo(
        @ColumnInfo(name = "bucket_id")      val bucketId: Long,
        @ColumnInfo(name = "bucket_name")    val bucketName: String,
        @ColumnInfo(name = "pending_count")  val pendingCount: Int,
        @ColumnInfo(name = "total_count")    val totalCount: Int,
        // Cover URI — prefers photo over video; empty string when album has no remaining files
        @ColumnInfo(name = "cover_uri")      val coverUri: String,
        // 1 when the best available cover is a video (no photos in album), 0 otherwise
        @ColumnInfo(name = "cover_is_video") val coverIsVideo: Boolean,
    ) {
        /** True when all files have been reviewed (no PENDING) but files still exist in the DB. */
        val isReviewed: Boolean get() = pendingCount == 0 && totalCount > 0
    }

    /**
     * Returns ALL albums that still have files in the DB (pending or already reviewed).
     * Albums disappear only when every file in them is physically deleted.
     * Cover prefers images over videos; falls back to video-only when no photos exist.
     * cover_is_video lets the UI choose the right thumbnail composable.
     */
    @Query("""
        SELECT
            bucket_id,
            bucket_name,
            SUM(CASE WHEN decision = 'PENDING' THEN 1 ELSE 0 END)  AS pending_count,
            COUNT(*)                                                 AS total_count,
            COALESCE(
                MIN(CASE WHEN mime_type LIKE 'image/%' AND decision = 'PENDING' THEN content_uri END),
                MIN(CASE WHEN mime_type LIKE 'image/%' THEN content_uri END),
                MIN(content_uri)
            ) AS cover_uri,
            CASE WHEN COALESCE(
                MIN(CASE WHEN mime_type LIKE 'image/%' AND decision = 'PENDING' THEN content_uri END),
                MIN(CASE WHEN mime_type LIKE 'image/%' THEN content_uri END)
            ) IS NULL THEN 1 ELSE 0 END AS cover_is_video
        FROM images
        WHERE bucket_name != ''
        GROUP BY bucket_id, bucket_name
        ORDER BY bucket_name ASC
    """)
    fun getAlbumsWithPending(): Flow<List<AlbumInfo>>

    /** Pending images for a specific album, ordered by date. */
    @Query("SELECT * FROM images WHERE bucket_id = :bucketId AND decision = 'PENDING' ORDER BY date_added ASC")
    fun getPendingImagesByAlbum(bucketId: Long): Flow<List<ImageEntity>>

    /** Total image count for a specific album (all decisions). */
    @Query("SELECT COUNT(*) FROM images WHERE bucket_id = :bucketId")
    fun getTotalCountByAlbum(bucketId: Long): Flow<Int>

    /** Images for a specific album filtered by decision — used in AlbumReview. */
    @Query("SELECT * FROM images WHERE bucket_id = :bucketId AND decision = :decision ORDER BY date_added ASC")
    suspend fun getByDecisionAndAlbum(bucketId: Long, decision: String): List<ImageEntity>

    // ── Backfill support (migration 2→3 — runs once, then never again) ──────

    /** Returns IDs of records that still need bucket info populated. */
    @Query("SELECT id FROM images WHERE bucket_id = 0")
    suspend fun getIdsMissingBucketInfo(): List<Long>

    @Query("UPDATE images SET bucket_id = :bucketId, bucket_name = :bucketName WHERE id = :id")
    suspend fun updateBucketInfo(id: Long, bucketId: Long, bucketName: String)

    // ── Month summaries ───────────────────────────────────────────────────────

    data class MonthSummary(
        @ColumnInfo(name = "month_key") val monthKey: String,
        val total: Int,
        val pending: Int,
        val kept: Int,
        val deleted: Int,
        val bookmarked: Int,
    )

    @Query("""
        SELECT
            month_key,
            COUNT(*) AS total,
            SUM(CASE WHEN decision = 'PENDING'  THEN 1 ELSE 0 END) AS pending,
            SUM(CASE WHEN decision = 'KEEP'     THEN 1 ELSE 0 END) AS kept,
            SUM(CASE WHEN decision = 'DELETE'   THEN 1 ELSE 0 END) AS deleted,
            SUM(CASE WHEN decision = 'BOOKMARK' THEN 1 ELSE 0 END) AS bookmarked
        FROM images
        GROUP BY month_key
        ORDER BY month_key DESC
    """)
    suspend fun getMonthSummaries(): List<MonthSummary>
}
