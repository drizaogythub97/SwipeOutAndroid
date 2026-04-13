package com.swipeout.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_menus")
data class MonthlyMenuEntity(
    @PrimaryKey val key: String,          // "2024-03"
    val title: String,                    // "Março 2024"
    @ColumnInfo(name = "total_count")     val totalCount: Int,
    @ColumnInfo(name = "pending_count")   val pendingCount: Int,
    @ColumnInfo(name = "kept_count")      val keptCount: Int,
    @ColumnInfo(name = "deleted_count")   val deletedCount: Int,
    @ColumnInfo(name = "bookmarked_count")val bookmarkedCount: Int,
    @ColumnInfo(name = "is_completed")    val isCompleted: Boolean = false,
    @ColumnInfo(name = "cover_uri")       val coverUri: String = "",
) {
    val reviewedCount get() = keptCount + deletedCount + bookmarkedCount
}
