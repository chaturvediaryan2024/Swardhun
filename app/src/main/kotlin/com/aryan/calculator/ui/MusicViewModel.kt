package com.aryan.calculator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aryan.calculator.data.MusicRepository
import com.aryan.calculator.data.local.PlaylistWithSongCount
import com.aryan.calculator.data.local.UserPreferences
import com.aryan.calculator.data.local.UserProfile
import com.aryan.calculator.data.model.Song
import com.aryan.calculator.playback.PlaybackState
import com.aryan.calculator.playback.PlayerController
import kotlinx.coroutines.Job
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

    fun clearToast() {
        _toastMessage.value = null
    }

    private val _home = MutableStateFlow<List<Song>>(emptyList())
    val home: StateFlow<List<Song>> = _home.asStateFlow()

    private val _isLoadingHome = MutableStateFlow(false)
    val isLoadingHome: StateFlow<Boolean> = _isLoadingHome.asStateFlow()

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

    private var searchJob: Job? = null

    init {
        loadHome()
        observeCurrentSong()
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

    fun playFrom(list: List<Song>, song: Song) {
        val index = list.indexOfFirst { it.id == song.id }
        if (index >= 0) playerController.playQueue(list, index)
    }

    fun toggleDownload(song: Song) {
        viewModelScope.launch {
            val isDownloaded = repository.isDownloaded(song.id)
            if (isDownloaded) {
                repository.removeDownload(song.id)
            } else {
                _downloadingIds.value = _downloadingIds.value + song.id
                DownloadNotificationHelper.showDownloadStarted(appContext, song.title)
                try {
                    repository.download(song)
                    DownloadNotificationHelper.showDownloadComplete(appContext, song.title)
                } catch (e: Exception) {
                    DownloadNotificationHelper.showDownloadFailed(appContext, song.title)
                } finally {
                    _downloadingIds.value = _downloadingIds.value - song.id
                }
            }
            val flipped = !song.downloaded
            _home.value = _home.value.map { if (it.id == song.id) it.copy(downloaded = flipped) else it }
            _searchResults.value = _searchResults.value.map { if (it.id == song.id) it.copy(downloaded = flipped) else it }
        }
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

    // Playlist functions
    val playlists = repository.observePlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun createPlaylistAndAddSong(name: String, song: Song) {
        viewModelScope.launch {
            val playlistId = repository.createPlaylist(name)
            repository.addSongToPlaylist(playlistId, song)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, song)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun getPlaylistSongs(playlistId: Long) = repository.observePlaylistSongs(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
