package com.swipeout.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.swipeout.data.db.dao.DeletionEventDao
import com.swipeout.data.db.dao.ImageDao
import com.swipeout.data.db.dao.MonthlyMenuDao
import com.swipeout.data.db.entity.DeletionEventEntity
import com.swipeout.data.db.entity.ImageEntity
import com.swipeout.data.db.entity.MonthlyMenuEntity

@Database(
    entities = [ImageEntity::class, MonthlyMenuEntity::class, DeletionEventEntity::class],
    version  = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
    abstract fun monthlyMenuDao(): MonthlyMenuDao
    abstract fun deletionEventDao(): DeletionEventDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS deletion_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                timestamp_ms INTEGER NOT NULL,
                file_count INTEGER NOT NULL,
                bytes_freed INTEGER NOT NULL
            )"""
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Check existing columns before adding — prevents crash if migration runs twice
        val cursor = database.query("PRAGMA table_info(images)")
        val existingCols = mutableSetOf<String>()
        val nameIdx = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) existingCols += cursor.getString(nameIdx)
        cursor.close()

        if ("bucket_id" !in existingCols)
            database.execSQL("ALTER TABLE images ADD COLUMN bucket_id INTEGER NOT NULL DEFAULT 0")
        if ("bucket_name" !in existingCols)
            database.execSQL("ALTER TABLE images ADD COLUMN bucket_name TEXT NOT NULL DEFAULT ''")

        // CREATE INDEX IF NOT EXISTS is already idempotent
        database.execSQL("CREATE INDEX IF NOT EXISTS index_images_bucket_id ON images(bucket_id)")
    }
}
