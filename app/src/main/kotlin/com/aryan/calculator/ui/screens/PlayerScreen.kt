package com.aryan.calculator.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil.compose.AsyncImage
import com.aryan.calculator.data.model.Song
import com.aryan.calculator.ui.components.SongOptionsSheet
import com.aryan.calculator.playback.PlaybackState
import com.aryan.calculator.playback.RepeatMode
import com.aryan.calculator.ui.MusicViewModel
import com.aryan.calculator.ui.theme.AccentBlue
import com.aryan.calculator.ui.theme.AccentLime
import com.aryan.calculator.ui.theme.AccentPurple
import com.aryan.calculator.ui.theme.BgDark
import com.aryan.calculator.ui.theme.GradientBlue
import com.aryan.calculator.ui.theme.GradientPurple
import com.aryan.calculator.ui.theme.HeartRed
import com.aryan.calculator.ui.theme.ProgressBlue
import com.aryan.calculator.ui.theme.ShuffleGreen
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    state: PlaybackState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onTick: () -> Unit,
    onClose: () -> Unit,
    onShuffle: () -> Unit = {},
    onRepeat: () -> Unit = {},
    onLike: () -> Unit = {},
    isLiked: Boolean = false,
    onShowQueue: () -> Unit = {},
    onDownload: () -> Unit = {},
    onAddToQueue: () -> Unit = {},
    onShare: () -> Unit = {},
    onViewArtist: () -> Unit = {},
    downloadStatus: MusicViewModel.DownloadStatus? = null,
    onSetSpeed: (Float) -> Unit = {},
    currentSpeed: Float = 1f
) {
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    val song = state.currentSong

    LaunchedEffect(state.isPlaying) {
        while (state.isPlaying) {
            onTick()
            delay(500)
        }
    }

    val playButtonScale by animateFloatAsState(
        targetValue = if (state.isPlaying) 1f else 0.95f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "playScale"
    )

    val artworkScale by animateFloatAsState(
        targetValue = if (state.isPlaying) 1f else 0.92f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "artworkScale"
    )

    // Spin the circular artwork like a vinyl record -- only while playing.
    // Pausing freezes it at the current angle; resuming continues from there.
    val artworkRotation = remember { Animatable(0f) }
    LaunchedEffect(state.isPlaying) {
        if (state.isPlaying) {
            artworkRotation.animateTo(
                targetValue = artworkRotation.value + 360_000f,
                animationSpec = tween(durationMillis = 20_000 * 1000, easing = LinearEasing)
            )
        } else {
            artworkRotation.stop()
        }
    }

    val likeColor by animateColorAsState(
        targetValue = if (isLiked) HeartRed else Color.White,
        animationSpec = tween(300),
        label = "likeColor"
    )

    val shuffleColor by animateColorAsState(
        targetValue = if (state.shuffleEnabled) ShuffleGreen else Color.White.copy(alpha = 0.6f),
        animationSpec = tween(300),
        label = "shuffleColor"
    )

    val repeatColor by animateColorAsState(
        targetValue = if (state.repeatMode != RepeatMode.OFF) AccentLime else Color.White.copy(alpha = 0.6f),
        animationSpec = tween(300),
        label = "repeatColor"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = song?.artwork,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
                .scale(1.2f)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.5f),
                            BgDark.copy(alpha = 0.85f),
                            BgDark.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = song?.album?.takeIf { it.isNotBlank() } ?: "Musify",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = { showOptionsMenu = true }) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "More",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .aspectRatio(1f)
                    .scale(artworkScale),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = song?.artwork,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(40.dp, CircleShape)
                        .clip(CircleShape)
                        .rotate(artworkRotation.value)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song?.title ?: "",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = song?.artist ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onLike) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Like",
                        tint = likeColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val durationF = state.durationMs.toFloat().coerceAtLeast(1f)
            Slider(
                value = state.positionMs.toFloat().coerceIn(0f, durationF),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..durationF,
                colors = SliderDefaults.colors(
                    thumbColor = AccentLime,
                    activeTrackColor = AccentLime,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    formatMs(state.positionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    "-${formatMs(state.durationMs - state.positionMs)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onShuffle) {
                    Icon(
                        Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = shuffleColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(playButtonScale)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(AccentLime),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
                IconButton(onClick = onRepeat) {
                    Icon(
                        imageVector = if (state.repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = repeatColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Rounded.Share,
                        contentDescription = "Share",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Playback speed
                TextButton(onClick = { showSpeedDialog = true }) {
                    Text(
                        text = "${currentSpeed}x",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (currentSpeed != 1f) AccentLime else Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = onShowQueue) {
                    Icon(
                        Icons.Rounded.QueueMusic,
                        contentDescription = "Queue",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Download notification in player screen
        downloadStatus?.let { status ->
            DownloadNotification(
                status = status,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 60.dp)
            )
        }
    }

    if (showOptionsMenu && song != null) {
        SongOptionsSheet(
            song = song,
            isLiked = isLiked,
            onDismiss = { showOptionsMenu = false },
            onDownload = {
                onDownload()
                showOptionsMenu = false
            },
            onLike = {
                onLike()
                showOptionsMenu = false
            },
            onAddToQueue = {
                onAddToQueue()
                showOptionsMenu = false
            },
            onShare = {
                onShare()
                showOptionsMenu = false
            },
            onViewArtist = {
                onViewArtist()
                showOptionsMenu = false
            }
        )
    }

    if (showSpeedDialog) {
        val speeds = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            containerColor = BgDark,
            title = { Text("Playback Speed", color = Color.White) },
            text = {
                Column {
                    speeds.forEach { speed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetSpeed(speed)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${speed}x",
                                color = if (speed == currentSpeed) AccentBlue else Color.White,
                                fontWeight = if (speed == currentSpeed) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (speed == currentSpeed) {
                                Icon(Icons.Rounded.CheckCircle, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Close", color = AccentBlue)
                }
            }
        )
    }

}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
private fun DownloadNotification(
    status: MusicViewModel.DownloadStatus,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        status.isFailed -> Color(0xFFef4444)
        status.isRemoved -> Color(0xFFf97316)
        status.isComplete -> Color(0xFF22c55e)
        else -> Color(0xFF3b82f6)
    }

    val text = when {
        status.isFailed -> "Download failed"
        status.isRemoved -> "Removed: ${status.songTitle}"
        status.isComplete -> "Downloaded: ${status.songTitle}"
        else -> "Downloading: ${status.songTitle}"
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
        if (status.isComplete || status.isFailed || status.isRemoved) {
            Icon(
                if (status.isFailed) Icons.Rounded.Error else Icons.Rounded.CheckCircle,
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
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
