package com.swipeout.data.db.dao

import androidx.room.*
import com.swipeout.data.db.entity.MonthlyMenuEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyMenuDao {

    @Query("SELECT * FROM monthly_menus ORDER BY key DESC")
    fun getAllMenus(): Flow<List<MonthlyMenuEntity>>

    @Query("SELECT * FROM monthly_menus WHERE key = :key")
    suspend fun getMenu(key: String): MonthlyMenuEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(menus: List<MonthlyMenuEntity>)

    @Query("UPDATE monthly_menus SET is_completed = 1 WHERE key = :key")
    suspend fun markCompleted(key: String)

    // Only removes months that are NOT completed — completed months are kept forever
    // even after their images are deleted from the DB.
    @Query("DELETE FROM monthly_menus WHERE key NOT IN (:activeKeys) AND is_completed = 0")
    suspend fun removeStale(activeKeys: List<String>)
}
