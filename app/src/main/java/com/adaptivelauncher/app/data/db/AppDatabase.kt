package com.adaptivelauncher.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        FolderEntity::class,
        FolderApplicationEntity::class,
        LauncherApplicationEntity::class,
        HomeScreenItemEntity::class,
        UsageSummaryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun folderDao(): FolderDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun homeScreenDao(): HomeScreenDao
    abstract fun usageDao(): UsageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "adaptive_launcher.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
