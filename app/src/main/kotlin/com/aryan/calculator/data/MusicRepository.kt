package com.aryan.calculator.data

import com.aryan.calculator.data.local.LikedSongDao
import com.aryan.calculator.data.local.LikedSongEntity
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
    private val recentlyPlayedDao: RecentlyPlayedDao
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

    suspend fun toggleLike(song: Song) {
        if (likedSongDao.isLiked(song.id)) {
            likedSongDao.deleteById(song.id)
        } else {
            likedSongDao.insert(song.toLikedEntity())
        }
    }

    suspend fun isLiked(id: String): Boolean = likedSongDao.isLiked(id)

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
}
