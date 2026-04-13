package com.swipeout.data.di

import android.content.Context
import androidx.room.Room
import com.swipeout.data.db.AppDatabase
import com.swipeout.data.db.MIGRATION_1_2
import com.swipeout.data.db.MIGRATION_2_3
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "swipeout.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides fun provideImageDao(db: AppDatabase)         = db.imageDao()
    @Provides fun provideMonthlyMenuDao(db: AppDatabase)   = db.monthlyMenuDao()
    @Provides fun provideDeletionEventDao(db: AppDatabase) = db.deletionEventDao()
}
