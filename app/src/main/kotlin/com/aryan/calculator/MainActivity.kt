package com.aryan.calculator

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import com.aryan.calculator.data.model.Song
import com.aryan.calculator.ui.MusicViewModel
import com.aryan.calculator.ui.components.BottomNav
import com.aryan.calculator.ui.components.MiniPlayer
import com.aryan.calculator.ui.components.NavTab
import com.aryan.calculator.ui.components.SongOptionsSheet
import com.aryan.calculator.ui.screens.DownloadsScreen
import com.aryan.calculator.ui.screens.HomeScreen
import com.aryan.calculator.ui.screens.PlayerScreen
import com.aryan.calculator.ui.screens.ProfileScreen
import com.aryan.calculator.ui.screens.SearchScreen
import com.aryan.calculator.ui.screens.SplashScreen
import com.aryan.calculator.ui.screens.UpdateScreen
import com.aryan.calculator.data.AppUpdate
import com.aryan.calculator.data.UpdateChecker
import com.aryan.calculator.ui.theme.BgDark
import com.aryan.calculator.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val app = application as CalculatorApp
        setContent {
            CalculatorTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgDark)
                ) {
                    val viewModel: MusicViewModel = viewModel(
                        factory = MusicViewModel.Factory(app.repository, app.playerController, app.userPreferences, app)
                    )
                    SwardhunApp(viewModel)
                }
            }
        }
    }
}

@Composable
private fun SwardhunApp(viewModel: MusicViewModel) {
    var showSplash by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(NavTab.HOME) }
    var showPlayer by remember { mutableStateOf(false) }
    var selectedSongForOptions by remember { mutableStateOf<Song?>(null) }
    var availableUpdate by remember { mutableStateOf<AppUpdate?>(null) }
    var checkingUpdate by remember { mutableStateOf(true) }

    // Check for update on app start
    LaunchedEffect(Unit) {
        availableUpdate = UpdateChecker.checkForUpdate()
        checkingUpdate = false
    }

    if (showSplash) {
        SplashScreen(onSplashComplete = { showSplash = false })
        return
    }

    // Show update screen if update available (forced update)
    if (availableUpdate != null) {
        UpdateScreen(update = availableUpdate!!)
        return
    }

    // Show loading while checking for update
    if (checkingUpdate) {
        return
    }

    val home by viewModel.home.collectAsState()
    val isLoadingHome by viewModel.isLoadingHome.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val downloads by viewModel.downloads.collectAsState()
    val likedSongs by viewModel.likedSongs.collectAsState()
    val likedIds by viewModel.likedIds.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    val currentSong = playbackState.currentSong
    val isCurrentLiked = viewModel.isCurrentSongLiked()

    // Back button handling
    BackHandler(enabled = showPlayer || selectedTab != NavTab.HOME) {
        when {
            showPlayer -> showPlayer = false
            selectedTab != NavTab.HOME -> selectedTab = NavTab.HOME
        }
    }

    if (showPlayer && currentSong != null) {
        PlayerScreen(
            state = playbackState,
            onPlayPause = { viewModel.playerController.togglePlayPause() },
            onNext = { viewModel.playerController.next() },
            onPrevious = { viewModel.playerController.previous() },
            onSeek = { viewModel.playerController.seekTo(it) },
            onTick = { viewModel.playerController.tickPosition() },
            onClose = { showPlayer = false },
            onShuffle = { viewModel.playerController.toggleShuffle() },
            onRepeat = { viewModel.playerController.cycleRepeatMode() },
            onLike = { currentSong.let { viewModel.toggleLike(it) } },
            isLiked = isCurrentLiked,
            onDownload = { viewModel.toggleDownload(currentSong) },
            onAddToQueue = { viewModel.addToQueue(currentSong) },
            onShare = { },
            onViewArtist = {
                viewModel.searchArtist(currentSong.artist.split(",").first().trim())
                selectedTab = NavTab.SEARCH
                showPlayer = false
            }
        )
        return
    }

    selectedSongForOptions?.let { song ->
        SongOptionsSheet(
            song = song,
            isLiked = likedIds.contains(song.id),
            onDismiss = { selectedSongForOptions = null },
            onDownload = { viewModel.toggleDownload(song) },
            onLike = { viewModel.toggleLike(song) },
            onAddToQueue = { viewModel.addToQueue(song) },
            onShare = { },
            onViewArtist = {
                viewModel.searchArtist(song.artist.split(",").first().trim())
                selectedTab = NavTab.SEARCH
            }
        )
    }

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            Column {
                currentSong?.let { song ->
                    val progress = if (playbackState.durationMs > 0) {
                        playbackState.positionMs.toFloat() / playbackState.durationMs.toFloat()
                    } else 0f
                    MiniPlayer(
                        song = song,
                        isPlaying = playbackState.isPlaying,
                        onClick = { showPlayer = true },
                        onPlayPause = { viewModel.playerController.togglePlayPause() },
                        onNext = { viewModel.playerController.next() },
                        progress = progress
                    )
                }
                BottomNav(selected = selectedTab, onSelect = { selectedTab = it })
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BgDark)
        ) {
            when (selectedTab) {
                NavTab.HOME -> HomeScreen(
                    songs = home,
                    isLoading = isLoadingHome,
                    onSongClick = { viewModel.playFrom(home, it) },
                    onDownloadToggle = { selectedSongForOptions = it },
                    currentPlayingSongId = currentSong?.id,
                    recentlyPlayed = recentlyPlayed,
                    onDownloadClick = { viewModel.toggleDownload(it) },
                    userPhotoUri = userProfile.photoUri,
                    onProfileClick = { selectedTab = NavTab.PROFILE }
                )
                NavTab.SEARCH -> SearchScreen(
                    query = searchQuery,
                    results = searchResults,
                    isSearching = isSearching,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onSongClick = { viewModel.playFrom(searchResults, it) },
                    onDownloadToggle = { selectedSongForOptions = it },
                    currentPlayingSongId = currentSong?.id,
                    onDownloadClick = { viewModel.toggleDownload(it) }
                )
                NavTab.LIBRARY -> DownloadsScreen(
                    songs = downloads,
                    onSongClick = { viewModel.playFrom(downloads, it) },
                    onDeleteSong = { viewModel.toggleDownload(it) },
                    onPlayAll = { if (downloads.isNotEmpty()) viewModel.playFrom(downloads, downloads.first()) },
                    onShufflePlay = {
                        if (downloads.isNotEmpty()) {
                            viewModel.playerController.playQueue(downloads, 0, shuffle = true)
                        }
                    },
                    currentPlayingSongId = currentSong?.id
                )
                NavTab.PROFILE -> ProfileScreen(
                    profile = userProfile,
                    onNameChange = { viewModel.setUserName(it) },
                    onPhotoChange = { viewModel.setUserPhoto(it) }
                )
            }
        }
    }
}
