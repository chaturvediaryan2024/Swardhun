package com.aryan.calculator.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class PlaylistWithSongCount(
    val playlistId: Long,
    val name: String,
    val createdAt: Long,
    val songCount: Int
)

@Dao
interface PlaylistDao {

    @Query("SELECT p.playlistId, p.name, p.createdAt, COUNT(ps.songId) as songCount FROM playlists p LEFT JOIN playlist_songs ps ON p.playlistId = ps.playlistId GROUP BY p.playlistId ORDER BY p.createdAt DESC")
    fun observeAllWithCount(): Flow<List<PlaylistWithSongCount>>

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    suspend fun getAll(): List<PlaylistEntity>

    @Query("SELECT * FROM playlists WHERE playlistId = :id")
    suspend fun getById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE playlistId = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("UPDATE playlists SET name = :name WHERE playlistId = :id")
    suspend fun renamePlaylist(id: Long, name: String)

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY addedAt DESC")
    fun observeSongsInPlaylist(playlistId: Long): Flow<List<PlaylistSongEntity>>

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY addedAt DESC")
    suspend fun getSongsInPlaylist(playlistId: Long): List<PlaylistSongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(song: PlaylistSongEntity)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId)")
    suspend fun isSongInPlaylist(playlistId: Long, songId: String): Boolean
}
