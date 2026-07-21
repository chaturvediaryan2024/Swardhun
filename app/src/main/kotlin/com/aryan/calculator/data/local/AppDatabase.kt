package com.aryan.calculator.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DownloadedSongEntity::class,
        LikedSongEntity::class,
        RecentlyPlayedEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun downloadedSongDao(): DownloadedSongDao
    abstract fun likedSongDao(): LikedSongDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "musify.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
