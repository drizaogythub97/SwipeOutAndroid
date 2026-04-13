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
        @ColumnInfo(name = "bucket_id")     val bucketId: Long,
        @ColumnInfo(name = "bucket_name")   val bucketName: String,
        @ColumnInfo(name = "pending_count") val pendingCount: Int,
    )

    /**
     * Returns albums that have at least one PENDING image.
     * Uses bucket_id index — O(log n). Result is a small list (typically 10–30 albums).
     * Only active when HomeScreen album filter is open.
     */
    @Query("""
        SELECT
            bucket_id,
            bucket_name,
            SUM(CASE WHEN decision = 'PENDING' THEN 1 ELSE 0 END) AS pending_count
        FROM images
        WHERE bucket_name != ''
        GROUP BY bucket_id, bucket_name
        HAVING pending_count > 0
        ORDER BY bucket_name ASC
    """)
    fun getAlbumsWithPending(): Flow<List<AlbumInfo>>

    /**
     * Returns the month keys that have PENDING images in a specific album.
     * Used to filter the HomeScreen month list — only runs when album filter is active.
     */
    @Query("SELECT DISTINCT month_key FROM images WHERE bucket_id = :bucketId AND decision = 'PENDING'")
    fun getMonthKeysForAlbum(bucketId: Long): Flow<List<String>>

    // ── Backfill support (migration 2→3) ─────────────────────────────────────

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
