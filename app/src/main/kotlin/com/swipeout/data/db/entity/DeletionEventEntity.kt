package com.swipeout.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deletion_events")
data class DeletionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "timestamp_ms") val timestampMs: Long,
    @ColumnInfo(name = "file_count")   val fileCount: Int,
    @ColumnInfo(name = "bytes_freed")  val bytesFreed: Long,
)
