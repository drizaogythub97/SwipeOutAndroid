package com.swipeout.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.swipeout.data.db.entity.DeletionEventEntity

@Dao
interface DeletionEventDao {

    @Insert
    suspend fun insert(event: DeletionEventEntity)

    @Query("SELECT COALESCE(SUM(file_count), 0) FROM deletion_events WHERE timestamp_ms > :since")
    suspend fun totalFilesSince(since: Long): Long

    @Query("SELECT COALESCE(SUM(bytes_freed), 0) FROM deletion_events WHERE timestamp_ms > :since")
    suspend fun totalBytesSince(since: Long): Long
}
