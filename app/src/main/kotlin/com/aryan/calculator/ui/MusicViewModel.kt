package com.aryan.calculator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aryan.calculator.data.MusicRepository
import com.aryan.calculator.data.local.UserPreferences
import com.aryan.calculator.data.local.UserProfile
import com.aryan.calculator.data.model.Song
import com.aryan.calculator.playback.PlaybackState
import com.aryan.calculator.playback.PlayerController
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import android.content.Context
import com.aryan.calculator.data.DownloadNotificationHelper

class MusicViewModel(
    private val repository: MusicRepository,
    val playerController: PlayerController,
    private val userPreferences: UserPreferences,
    private val appContext: Context
) : ViewModel() {

    private val _downloadingIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingIds: StateFlow<Set<String>> = _downloadingIds.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _downloadStatus = MutableStateFlow<DownloadStatus?>(null)
    val downloadStatus: StateFlow<DownloadStatus?> = _downloadStatus.asStateFlow()

    data class DownloadStatus(val songTitle: String, val isComplete: Boolean, val isFailed: Boolean = false, val isRemoved: Boolean = false)

    fun clearToast() {
        _toastMessage.value = null
    }

    private val _home = MutableStateFlow<List<Song>>(emptyList())
    val home: StateFlow<List<Song>> = _home.asStateFlow()

    private val _isLoadingHome = MutableStateFlow(false)
    val isLoadingHome: StateFlow<Boolean> = _isLoadingHome.asStateFlow()

    // Home chip categories: 0=All (uses _home), 1=Trending, 2=New Release, 3=Top.
    // Loaded lazily the first time each chip is opened, then cached.
    private val _categorySongs = MutableStateFlow<Map<Int, List<Song>>>(emptyMap())
    val categorySongs: StateFlow<Map<Int, List<Song>>> = _categorySongs.asStateFlow()

    private val _loadingCategory = MutableStateFlow<Int?>(null)
    val loadingCategory: StateFlow<Int?> = _loadingCategory.asStateFlow()

    fun loadCategory(index: Int) {
        if (index == 0) return
        if (_categorySongs.value.containsKey(index)) return
        viewModelScope.launch {
            _loadingCategory.value = index
            val langs = _selectedLanguages.value
            val songs = when (index) {
                1 -> repository.trending(langs)
                2 -> repository.newReleases(langs)
                3 -> repository.topCharts(langs)
                else -> emptyList()
            }
            _categorySongs.value = _categorySongs.value + (index to songs)
            _loadingCategory.value = null
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    val downloads: StateFlow<List<Song>> = repository.observeDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedSongs: StateFlow<List<Song>> = repository.observeLikedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedIds: StateFlow<Set<String>> = repository.observeLikedIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val recentlyPlayed: StateFlow<List<Song>> = repository.observeRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playbackState: StateFlow<PlaybackState> = playerController.state

    val userProfile: StateFlow<UserProfile> = userPreferences.profile

    private val _selectedLanguages = MutableStateFlow(userPreferences.getSelectedLanguages())
    val selectedLanguages: StateFlow<Set<String>> = _selectedLanguages.asStateFlow()

    val audioQuality: StateFlow<String> = userPreferences.audioQuality

    fun setAudioQuality(quality: String) {
        userPreferences.setAudioQuality(quality)
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            downloads.value.forEach { repository.removeDownload(it.id) }
            _toastMessage.value = "All downloads removed"
        }
    }

    private var searchJob: Job? = null

    fun setPlaybackSpeed(speed: Float) {
        playerController.setPlaybackSpeed(speed)
    }

    data class TrendingEntry(val name: String, val type: String, val imageUrl: String = "")

    private val _trending = MutableStateFlow(
        listOf(
            TrendingEntry("Arijit Singh", "Artist"),
            TrendingEntry("Diljit Dosanjh", "Artist"),
            TrendingEntry("AP Dhillon", "Artist"),
            TrendingEntry("Atif Aslam", "Artist"),
            TrendingEntry("Shreya Ghoshal", "Artist"),
            TrendingEntry("Jubin Nautiyal", "Artist")
        )
    )
    val trending: StateFlow<List<TrendingEntry>> = _trending.asStateFlow()

    // Search "Browse All" categories (Spotify-style cards with a corner image).
    data class BrowseCategory(
        val name: String,
        val query: String,
        val color: Long,
        val imageUrl: String = ""
    )

    private val _categories = MutableStateFlow(
        listOf(
            BrowseCategory("Made For You", "top hindi hits 2024", 0xFF7C4DFF),
            BrowseCategory("New Releases", "new hindi songs 2024", 0xFF689F38),
            BrowseCategory("Hindi", "bollywood superhits", 0xFFEC407A),
            BrowseCategory("Tamil", "tamil hit songs", 0xFFD84315),
            BrowseCategory("Pop", "english pop hits", 0xFF00897B),
            BrowseCategory("Charts", "top trending india", 0xFF5E35B1),
            BrowseCategory("Punjabi", "punjabi hits 2024", 0xFF1E88E5),
            BrowseCategory("Romance", "romantic hindi songs", 0xFFC2185B),
            BrowseCategory("Party", "party dance songs", 0xFF8E24AA),
            BrowseCategory("Workout", "workout gym songs", 0xFF2E7D32)
        )
    )
    val categories: StateFlow<List<BrowseCategory>> = _categories.asStateFlow()

    init {
        loadHome()
        observeCurrentSong()
        loadTrendingImages()
        loadCategoryImages()
    }

    private fun loadCategoryImages() {
        viewModelScope.launch {
            val updated = _categories.value.map { cat ->
                async { cat.copy(imageUrl = repository.getCategoryImage(cat.query) ?: "") }
            }.map { it.await() }
            _categories.value = updated
        }
    }

    private fun loadTrendingImages() {
        viewModelScope.launch {
            val updated = _trending.value.map { entry ->
                async { entry.copy(imageUrl = repository.getEntityImage(entry.name) ?: "") }
            }.map { it.await() }
            _trending.value = updated
        }
    }

    private fun observeCurrentSong() {
        viewModelScope.launch {
            playerController.state.collect { state ->
                state.currentSong?.let { song ->
                    repository.addToRecentlyPlayed(song)
                }
            }
        }
    }

    fun loadHome() {
        viewModelScope.launch {
            _isLoadingHome.value = true
            _home.value = repository.home(_selectedLanguages.value)
            _isLoadingHome.value = false
        }
    }

    fun setUserName(name: String) {
        userPreferences.setName(name)
    }

    fun setUserPhoto(uri: String) {
        userPreferences.setPhotoUri(uri)
    }

    fun setLanguages(languages: Set<String>) {
        userPreferences.setSelectedLanguages(languages)
        _selectedLanguages.value = languages
        loadHome()
    }

    fun addToQueue(song: Song) {
        playerController.addToQueue(song)
    }

    fun searchArtist(artistName: String) {
        _searchQuery.value = artistName
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            _searchResults.value = repository.search(artistName)
            _isSearching.value = false
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _isSearching.value = false
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            _isSearching.value = true
            _searchResults.value = repository.search(query)
            _isSearching.value = false
        }
    }

    fun playAll(songs: List<Song>, shuffle: Boolean = false) {
        if (songs.isEmpty()) return
        viewModelScope.launch {
            val refreshed = repository.refreshStreamUrls(songs)
            playerController.playQueue(refreshed, 0, shuffle = shuffle)
        }
    }

    fun playFrom(list: List<Song>, song: Song, refresh: Boolean = false) {
        val index = list.indexOfFirst { it.id == song.id }
        if (index < 0) return
        if (!refresh) {
            playerController.playQueue(list, index)
            return
        }
        // Stored collections (playlist / liked / recently played) may hold expired
        // JioSaavn URLs -- re-resolve fresh ones before playing.
        viewModelScope.launch {
            val refreshed = repository.refreshStreamUrls(list)
            playerController.playQueue(refreshed, index)
        }
    }

    fun downloadSong(song: Song) {
        // If already downloading or already downloaded, ignore
        if (_downloadingIds.value.contains(song.id)) {
            return
        }

        viewModelScope.launch {
            val isDownloaded = repository.isDownloaded(song.id)
            if (isDownloaded) {
                // Already downloaded - show message
                _toastMessage.value = "Already downloaded"
                return@launch
            }

            // Download song
            _downloadingIds.value = _downloadingIds.value + song.id
            _downloadStatus.value = DownloadStatus(song.title, isComplete = false)
            try {
                repository.download(song)
                _downloadStatus.value = DownloadStatus(song.title, isComplete = true)
                _home.value = _home.value.map { if (it.id == song.id) it.copy(downloaded = true) else it }
                _searchResults.value = _searchResults.value.map { if (it.id == song.id) it.copy(downloaded = true) else it }
                delay(2000)
            } catch (e: Exception) {
                _downloadStatus.value = DownloadStatus(song.title, isComplete = false, isFailed = true)
                delay(2000)
            } finally {
                _downloadingIds.value = _downloadingIds.value - song.id
                _downloadStatus.value = null
            }
        }
    }

    fun removeSong(song: Song) {
        viewModelScope.launch {
            repository.removeDownload(song.id)
            _downloadStatus.value = DownloadStatus(song.title, isComplete = true, isFailed = false, isRemoved = true)
            _home.value = _home.value.map { if (it.id == song.id) it.copy(downloaded = false) else it }
            _searchResults.value = _searchResults.value.map { if (it.id == song.id) it.copy(downloaded = false) else it }
            delay(1500)
            _downloadStatus.value = null
        }
    }

    // Keep for backward compatibility but now just calls downloadSong
    fun toggleDownload(song: Song) {
        downloadSong(song)
    }

    fun clearDownloadStatus() {
        _downloadStatus.value = null
    }

    fun toggleLike(song: Song) {
        viewModelScope.launch {
            repository.toggleLike(song)
            val flipped = !song.isLiked
            _home.value = _home.value.map { if (it.id == song.id) it.copy(isLiked = flipped) else it }
            _searchResults.value = _searchResults.value.map { if (it.id == song.id) it.copy(isLiked = flipped) else it }
        }
    }

    fun isCurrentSongLiked(): Boolean {
        val currentId = playbackState.value.currentSong?.id ?: return false
        return likedIds.value.contains(currentId)
    }

    class Factory(
        private val repository: MusicRepository,
        private val playerController: PlayerController,
        private val userPreferences: UserPreferences,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(repository, playerController, userPreferences, appContext) as T
        }
    }
}
