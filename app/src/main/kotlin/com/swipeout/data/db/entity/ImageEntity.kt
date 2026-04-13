package com.swipeout.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "images",
    indices = [Index("month_key"), Index("decision"), Index("bucket_id")]
)
data class ImageEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "content_uri") val contentUri: String,
    @ColumnInfo(name = "month_key")   val monthKey: String,
    @ColumnInfo(name = "date_added")  val dateAdded: Long,      // epoch seconds
    @ColumnInfo(name = "size_bytes")  val sizeBytes: Long,
    @ColumnInfo(name = "mime_type")   val mimeType: String,
    @ColumnInfo(name = "duration_ms") val durationMs: Long = 0, // videos only
    val width: Int,
    val height: Int,
    val decision: String = PENDING,                             // PENDING / KEEP / DELETE / BOOKMARK
    @ColumnInfo(name = "bucket_id")   val bucketId: Long   = 0L,
    @ColumnInfo(name = "bucket_name") val bucketName: String = "",
) {
    val isVideo get() = mimeType.startsWith("video/")

    companion object {
        const val PENDING  = "PENDING"
        const val KEEP     = "KEEP"
        const val DELETE   = "DELETE"
        const val BOOKMARK = "BOOKMARK"
    }
}
