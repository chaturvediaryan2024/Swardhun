package com.aryan.calculator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedSongDao {

    @Query("SELECT * FROM downloaded_songs ORDER BY downloadedAt DESC")
    fun observeAll(): Flow<List<DownloadedSongEntity>>

    @Query("SELECT id FROM downloaded_songs")
    suspend fun getAllIds(): List<String>

    @Query("SELECT * FROM downloaded_songs WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): DownloadedSongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DownloadedSongEntity)

    @Query("DELETE FROM downloaded_songs WHERE id = :id")
    suspend fun deleteById(id: String)
}
