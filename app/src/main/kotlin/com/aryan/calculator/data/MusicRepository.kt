package com.aryan.calculator.data

import com.aryan.calculator.data.local.LikedSongDao
import com.aryan.calculator.data.local.LikedSongEntity
import com.aryan.calculator.data.local.PlaylistDao
import com.aryan.calculator.data.local.PlaylistEntity
import com.aryan.calculator.data.local.PlaylistSongEntity
import com.aryan.calculator.data.local.PlaylistWithSongCount
import com.aryan.calculator.data.local.RecentlyPlayedDao
import com.aryan.calculator.data.local.RecentlyPlayedEntity
import com.aryan.calculator.data.model.Song
import com.aryan.calculator.data.network.JioSaavnApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class MusicRepository(
    private val downloadManager: DownloadManager,
    private val likedSongDao: LikedSongDao,
    private val recentlyPlayedDao: RecentlyPlayedDao,
    private val playlistDao: PlaylistDao
) {

    fun observeDownloads(): Flow<List<Song>> = downloadManager.observeDownloads()

    fun observeLikedSongs(): Flow<List<Song>> = likedSongDao.observeAll().map { list ->
        list.map { it.toSong().copy(isLiked = true) }
    }

    fun observeLikedIds(): Flow<Set<String>> = likedSongDao.observeAllIds().map { it.toSet() }

    fun observeRecentlyPlayed(): Flow<List<Song>> = recentlyPlayedDao.observeRecent().map { list ->
        list.map { it.toSong() }
    }

    suspend fun home(languages: Set<String> = setOf("hindi")): List<Song> =
        markDownloadedAndLiked(JioSaavnApi.home(languages))

    suspend fun search(query: String): List<Song> {
        if (query.isBlank()) return emptyList()
        return markDownloadedAndLiked(JioSaavnApi.search(query))
    }

    suspend fun isDownloaded(id: String): Boolean = downloadManager.isDownloaded(id)

    suspend fun toggleDownload(song: Song) {
        if (downloadManager.isDownloaded(song.id)) {
            downloadManager.remove(song.id)
        } else {
            downloadManager.download(song)
        }
    }

    suspend fun download(song: Song): Song = downloadManager.download(song)

    suspend fun removeDownload(id: String) = downloadManager.remove(id)

    suspend fun toggleLike(song: Song) {
        if (likedSongDao.isLiked(song.id)) {
            likedSongDao.deleteById(song.id)
        } else {
            likedSongDao.insert(song.toLikedEntity())
        }
    }

    suspend fun isLiked(id: String): Boolean = likedSongDao.isLiked(id)

    // Playlist functions
    fun observePlaylists(): Flow<List<PlaylistWithSongCount>> = playlistDao.observeAllWithCount()

    suspend fun getPlaylists(): List<PlaylistEntity> = playlistDao.getAll()

    suspend fun createPlaylist(name: String): Long = playlistDao.insertPlaylist(PlaylistEntity(name = name))

    suspend fun deletePlaylist(id: Long) = playlistDao.deletePlaylist(id)

    suspend fun renamePlaylist(id: Long, name: String) = playlistDao.renamePlaylist(id, name)

    fun observePlaylistSongs(playlistId: Long): Flow<List<Song>> =
        playlistDao.observeSongsInPlaylist(playlistId).map { list ->
            list.map { it.toSong() }
        }

    suspend fun addSongToPlaylist(playlistId: Long, song: Song) {
        playlistDao.addSongToPlaylist(song.toPlaylistSongEntity(playlistId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) =
        playlistDao.removeSongFromPlaylist(playlistId, songId)

    suspend fun addToRecentlyPlayed(song: Song) {
        recentlyPlayedDao.insert(song.toRecentlyPlayedEntity())
        recentlyPlayedDao.trimOld()
    }

    private suspend fun markDownloadedAndLiked(songs: List<Song>): List<Song> {
        val downloadedIds = downloadManager.downloadedIds()
        val likedIds = likedSongDao.getAllIds().toSet()
        return songs.map { song ->
            song.copy(
                downloaded = song.id in downloadedIds,
                isLiked = song.id in likedIds
            )
        }
    }

    private fun LikedSongEntity.toSong() = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        artwork = artwork,
        streamUrl = streamUrl
    )

    private fun RecentlyPlayedEntity.toSong() = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        artwork = artwork,
        streamUrl = streamUrl
    )

    private fun Song.toLikedEntity() = LikedSongEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        artwork = artwork,
        streamUrl = streamUrl
    )

    private fun Song.toRecentlyPlayedEntity() = RecentlyPlayedEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        artwork = artwork,
        streamUrl = streamUrl
    )

    private fun Song.toPlaylistSongEntity(playlistId: Long) = PlaylistSongEntity(
        playlistId = playlistId,
        songId = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        artwork = artwork,
        streamUrl = streamUrl
    )

    private fun PlaylistSongEntity.toSong() = Song(
        id = songId,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        artwork = artwork,
        streamUrl = streamUrl
    )
}
