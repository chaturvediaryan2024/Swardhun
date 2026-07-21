package com.aryan.calculator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyPlayedDao {

    @Query("SELECT * FROM recently_played ORDER BY playedAt DESC LIMIT 20")
    fun observeRecent(): Flow<List<RecentlyPlayedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecentlyPlayedEntity)

    @Query("DELETE FROM recently_played WHERE id NOT IN (SELECT id FROM recently_played ORDER BY playedAt DESC LIMIT 50)")
    suspend fun trimOld()
}
