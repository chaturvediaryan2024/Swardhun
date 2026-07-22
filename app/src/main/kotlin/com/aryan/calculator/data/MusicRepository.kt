package com.aryan.calculator.data

import com.aryan.calculator.data.local.LikedSongDao
import com.aryan.calculator.data.local.LikedSongEntity
import com.aryan.calculator.data.local.RecentlyPlayedDao
import com.aryan.calculator.data.local.RecentlyPlayedEntity
import com.aryan.calculator.data.model.Song
import com.aryan.calculator.data.network.JioSaavnApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

    suspend fun trending(languages: Set<String>): List<Song> =
        markDownloadedAndLiked(JioSaavnApi.trending(languages))

    suspend fun newReleases(languages: Set<String>): List<Song> =
        markDownloadedAndLiked(JioSaavnApi.newReleases(languages))

    suspend fun topCharts(languages: Set<String>): List<Song> =
        markDownloadedAndLiked(JioSaavnApi.topCharts(languages))

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

    suspend fun getEntityImage(name: String): String? = JioSaavnApi.getEntityImage(name)

    suspend fun getCategoryImage(query: String): String? =
        JioSaavnApi.searchSongs(query, 1).firstOrNull()?.artwork?.takeIf { it.isNotBlank() }

    /**
     * Return a copy of [song] with a fresh, playable stream URL. Local downloads
     * (file:// URLs) are returned untouched. If the network re-resolve fails the
     * original URL is kept so playback still attempts (better than nothing).
     */
    suspend fun refreshStreamUrl(song: Song): Song {
        if (!song.streamUrl.startsWith("http")) return song // local download
        val fresh = JioSaavnApi.getSongById(song.id)
        return if (fresh != null && fresh.streamUrl.startsWith("http")) {
            song.copy(streamUrl = fresh.streamUrl)
        } else song
    }

    suspend fun refreshStreamUrls(songs: List<Song>): List<Song> = coroutineScope {
        songs.map { async { refreshStreamUrl(it) } }.map { it.await() }
    }

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
