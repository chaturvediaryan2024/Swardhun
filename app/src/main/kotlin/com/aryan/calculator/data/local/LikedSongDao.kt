package com.aryan.calculator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LikedSongDao {

    @Query("SELECT * FROM liked_songs ORDER BY likedAt DESC")
    fun observeAll(): Flow<List<LikedSongEntity>>

    @Query("SELECT id FROM liked_songs")
    fun observeAllIds(): Flow<List<String>>

    @Query("SELECT id FROM liked_songs")
    suspend fun getAllIds(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM liked_songs WHERE id = :id)")
    suspend fun isLiked(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LikedSongEntity)

    @Query("DELETE FROM liked_songs WHERE id = :id")
    suspend fun deleteById(id: String)
}
