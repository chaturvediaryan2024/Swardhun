package com.aryan.calculator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
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
import com.aryan.calculator.ui.screens.LikedSongsScreen
import com.aryan.calculator.ui.screens.DownloadsScreen
import com.aryan.calculator.ui.screens.HistoryScreen
import com.aryan.calculator.ui.screens.QueueScreen
import com.aryan.calculator.ui.screens.HomeScreen
import com.aryan.calculator.ui.screens.PlayerScreen
import com.aryan.calculator.ui.screens.ProfileScreen
import com.aryan.calculator.ui.screens.SettingsScreen
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
    var showSettings by remember { mutableStateOf(false) }
    var profileRoute by remember { mutableStateOf<ProfileRoute?>(null) }
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

    // Show update screen only if an update is actually available (forced update).
    // We do NOT block the UI while the check is still running -- that used to show
    // a blank/black screen on slow networks. The app renders immediately and, if an
    // update is later found, this recomposes into the UpdateScreen.
    if (availableUpdate != null) {
        UpdateScreen(update = availableUpdate!!)
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
    val downloadingIds by viewModel.downloadingIds.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val trending by viewModel.trending.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val categorySongs by viewModel.categorySongs.collectAsState()
    val loadingCategory by viewModel.loadingCategory.collectAsState()
    val audioQuality by viewModel.audioQuality.collectAsState()
    val selectedLanguages by viewModel.selectedLanguages.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    val isDownloading = downloadingIds.isNotEmpty()

    val currentSong = playbackState.currentSong
    // Reactive: recomputes whenever likedIds or the current song changes so the
    // player's heart updates immediately when toggled.
    val isCurrentLiked = currentSong?.let { likedIds.contains(it.id) } ?: false

    // Back button handling
    BackHandler(enabled = showPlayer || selectedTab != NavTab.HOME) {
        when {
            showPlayer -> showPlayer = false
            selectedTab != NavTab.HOME -> {
                if (selectedTab == NavTab.SEARCH) viewModel.clearSearch()
                selectedTab = NavTab.HOME
            }
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
            onShare = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Listen to ${currentSong.title} by ${currentSong.artist} on Musify!"
                    )
                }
                runCatching { context.startActivity(Intent.createChooser(shareIntent, "Share via")) }
            },
            onShowQueue = {
                showPlayer = false
                profileRoute = ProfileRoute.QUEUE
            },
            onViewArtist = {
                viewModel.searchArtist(currentSong.artist.split(",").first().trim())
                selectedTab = NavTab.SEARCH
                showPlayer = false
            },
            downloadStatus = downloadStatus,
            onSetSpeed = { viewModel.setPlaybackSpeed(it) },
            currentSpeed = playbackState.playbackSpeed
        )
        return
    }

    // Settings screen
    if (showSettings) {
        BackHandler(enabled = true) { showSettings = false }
        SettingsScreen(
            audioQuality = audioQuality,
            onAudioQualityChange = { viewModel.setAudioQuality(it) },
            selectedLanguages = selectedLanguages,
            onLanguagesChange = { viewModel.setLanguages(it) },
            downloadCount = downloads.size,
            onClearDownloads = { viewModel.clearAllDownloads() },
            appVersion = "1.0",
            onBack = { showSettings = false }
        )
        return
    }

    // Profile sub-screens (Liked / Downloads / History / Queue)
    if (profileRoute != null) {
        BackHandler(enabled = true) { profileRoute = null }
        Box(modifier = Modifier.fillMaxSize()) {
            when (profileRoute) {
                ProfileRoute.LIKED -> LikedSongsScreen(
                    songs = likedSongs,
                    onSongClick = { viewModel.playFrom(likedSongs, it, refresh = true) },
                    onDownloadToggle = { selectedSongForOptions = it },
                    onPlayAll = { viewModel.playAll(likedSongs) },
                    onShufflePlay = { viewModel.playAll(likedSongs, shuffle = true) },
                    currentPlayingSongId = currentSong?.id
                )
                ProfileRoute.DOWNLOADS -> DownloadsScreen(
                    songs = downloads,
                    onSongClick = { viewModel.playFrom(downloads, it) },
                    onDeleteSong = { viewModel.removeSong(it) },
                    onPlayAll = { if (downloads.isNotEmpty()) viewModel.playFrom(downloads, downloads.first()) },
                    onShufflePlay = { viewModel.playAll(downloads, shuffle = true) },
                    currentPlayingSongId = currentSong?.id
                )
                ProfileRoute.HISTORY -> HistoryScreen(
                    songs = recentlyPlayed,
                    onBack = { profileRoute = null },
                    onSongClick = { viewModel.playFrom(recentlyPlayed, it, refresh = true) },
                    currentPlayingSongId = currentSong?.id
                )
                ProfileRoute.QUEUE -> QueueScreen(
                    songs = playbackState.queue,
                    currentIndex = playbackState.currentIndex,
                    onBack = { profileRoute = null },
                    onSongClick = { song ->
                        val idx = playbackState.queue.indexOfFirst { it.id == song.id }
                        if (idx >= 0) viewModel.playerController.playQueue(playbackState.queue, idx)
                    }
                )
                null -> {}
            }

            // Mini player so the user can see & control what's playing here too.
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
                    progress = progress,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                )
            }

            // Download/remove notification shows on THIS screen too.
            downloadStatus?.let { status ->
                DownloadSnackbar(
                    status = status,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 8.dp)
                )
            }
        }
        return
    }

    // Song options sheet
    if (selectedSongForOptions != null) {
        val song = selectedSongForOptions!!
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
                BottomNav(
                    selected = selectedTab,
                    onSelect = { newTab ->
                        // Leaving Search resets it so re-opening starts fresh.
                        if (selectedTab == NavTab.SEARCH && newTab != NavTab.SEARCH) {
                            viewModel.clearSearch()
                        }
                        selectedTab = newTab
                    }
                )
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
                    onSongClick = { song ->
                        if (home.any { it.id == song.id }) viewModel.playFrom(home, song)
                        else viewModel.playFrom(recentlyPlayed, song, refresh = true)
                    },
                    onOptionsClick = { selectedSongForOptions = it },
                    onLike = { viewModel.toggleLike(it) },
                    likedIds = likedIds,
                    currentPlayingSongId = currentSong?.id,
                    recentlyPlayed = recentlyPlayed,
                    userName = userProfile.name,
                    userPhotoUri = userProfile.photoUri,
                    onProfileClick = { selectedTab = NavTab.PROFILE },
                    categorySongs = categorySongs,
                    loadingCategory = loadingCategory,
                    onChipSelected = { viewModel.loadCategory(it) }
                )
                NavTab.SEARCH -> SearchScreen(
                    query = searchQuery,
                    results = searchResults,
                    isSearching = isSearching,
                    trending = trending,
                    categories = categories,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onSongClick = { viewModel.playFrom(searchResults, it) },
                    onDownloadToggle = { selectedSongForOptions = it },
                    currentPlayingSongId = currentSong?.id,
                    onDownloadClick = { viewModel.toggleDownload(it) }
                )
                NavTab.PROFILE -> ProfileScreen(
                    profile = userProfile,
                    likedCount = likedSongs.size,
                    downloadCount = downloads.size,
                    playedCount = recentlyPlayed.size,
                    listeningMinutes = recentlyPlayed.sumOf { it.duration } / 60,
                    onNameChange = { viewModel.setUserName(it) },
                    onPhotoChange = { viewModel.setUserPhoto(it) },
                    onOpenSettings = { showSettings = true },
                    onOpenLiked = { profileRoute = ProfileRoute.LIKED },
                    onOpenDownloads = { profileRoute = ProfileRoute.DOWNLOADS },
                    onOpenHistory = { profileRoute = ProfileRoute.HISTORY },
                    onOpenQueue = { profileRoute = ProfileRoute.QUEUE },
                    onOpenEqualizer = { openSystemEqualizer(context) },
                    onShareApp = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "🎵 Check out Musify - a free music streaming app!\n\n" +
                                    "Download the latest version here:\n" +
                                    "https://github.com/chaturvediaryan2024/Swardhun/releases/latest"
                            )
                        }
                        runCatching { context.startActivity(Intent.createChooser(shareIntent, "Share Musify")) }
                    }
                )
            }

            // Download status snackbar - show at top
            downloadStatus?.let { status ->
                DownloadSnackbar(
                    status = status,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun DownloadSnackbar(
    status: MusicViewModel.DownloadStatus,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        status.isFailed -> Color(0xFFef4444)
        status.isRemoved -> Color(0xFFf97316) // Orange for removed
        status.isComplete -> Color(0xFF22c55e)
        else -> Color(0xFF3b82f6)
    }

    val icon = when {
        status.isFailed -> Icons.Rounded.Error
        status.isRemoved -> Icons.Rounded.CheckCircle
        status.isComplete -> Icons.Rounded.CheckCircle
        else -> null
    }

    val text = when {
        status.isFailed -> "Download failed"
        status.isRemoved -> "Removed"
        status.isComplete -> "Downloaded"
        else -> "Downloading..."
    }

    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = status.songTitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}

enum class ProfileRoute { LIKED, DOWNLOADS, HISTORY, QUEUE }

/** Opens the device's system equalizer for our audio session (music). */
private fun openSystemEqualizer(context: Context) {
    val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        android.widget.Toast.makeText(
            context, "No equalizer available on this device", android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}
