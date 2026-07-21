package com.aryan.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aryan.calculator.data.local.PlaylistWithSongCount
import com.aryan.calculator.data.model.Song
import com.aryan.calculator.ui.theme.*

@Composable
fun LibraryScreen(
    downloads: List<Song>,
    playlists: List<PlaylistWithSongCount>,
    onSongClick: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShufflePlay: () -> Unit,
    currentPlayingSongId: String?,
    onCreatePlaylist: (String) -> Unit,
    onPlaylistClick: (Long) -> Unit,
    onDeletePlaylist: (Long) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Text(
            text = "Library",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            ),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                label = { Text("Downloads") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentTeal,
                    selectedLabelColor = Color.White,
                    containerColor = GlassBg,
                    labelColor = Color.White.copy(alpha = 0.7f)
                )
            )
            FilterChip(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                label = { Text("Playlists") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentPurple,
                    selectedLabelColor = Color.White,
                    containerColor = GlassBg,
                    labelColor = Color.White.copy(alpha = 0.7f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> DownloadsContent(
                songs = downloads,
                onSongClick = onSongClick,
                onDeleteSong = onDeleteSong,
                onPlayAll = onPlayAll,
                onShufflePlay = onShufflePlay,
                currentPlayingSongId = currentPlayingSongId
            )
            1 -> PlaylistsContent(
                playlists = playlists,
                onPlaylistClick = onPlaylistClick,
                onDeletePlaylist = onDeletePlaylist,
                onCreatePlaylist = { showCreateDialog = true }
            )
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false; newPlaylistName = "" },
            containerColor = GlassBg,
            title = { Text("Create Playlist", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Playlist name", color = Color.White.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentPurple,
                        cursorColor = AccentPurple
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(newPlaylistName.trim())
                            newPlaylistName = ""
                            showCreateDialog = false
                        }
                    }
                ) { Text("Create", color = AccentPurple) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false; newPlaylistName = "" }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}

@Composable
private fun DownloadsContent(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShufflePlay: () -> Unit,
    currentPlayingSongId: String?
) {
    if (songs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Rounded.CloudDownload,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color.White.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No downloads yet", color = Color.White.copy(alpha = 0.6f))
                Text("Download songs to listen offline", color = Color.White.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onPlayAll,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null, Modifier.size(20.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Play", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onShufflePlay,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Rounded.Shuffle, null, Modifier.size(20.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Shuffle", fontWeight = FontWeight.SemiBold)
                    }
                }
                Text(
                    "${songs.size} songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
            items(songs, key = { it.id }) { song ->
                DownloadedSongCard(
                    song = song,
                    onClick = { onSongClick(song) },
                    onDelete = { onDeleteSong(song) },
                    isPlaying = song.id == currentPlayingSongId
                )
            }
        }
    }
}

@Composable
private fun PlaylistsContent(
    playlists: List<PlaylistWithSongCount>,
    onPlaylistClick: (Long) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onCreatePlaylist: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
        item {
            // Create playlist button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCreatePlaylist)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(GradientPink, GradientPurple))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(16.dp))
                Text("Create Playlist", color = Color.White, fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (playlists.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.QueueMusic,
                            null,
                            Modifier.size(48.dp),
                            tint = Color.White.copy(alpha = 0.2f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("No playlists yet", color = Color.White.copy(alpha = 0.5f))
                    }
                }
            }
        } else {
            items(playlists, key = { it.playlistId }) { playlist ->
                PlaylistCard(
                    playlist = playlist,
                    onClick = { onPlaylistClick(playlist.playlistId) },
                    onDelete = { onDeletePlaylist(playlist.playlistId) }
                )
            }
        }
    }
}

@Composable
private fun DownloadedSongCard(
    song: Song,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    isPlaying: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AsyncImage(
                model = song.artwork,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("▶", color = AccentTeal, fontSize = 18.sp)
                }
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = if (isPlaying) AccentTeal else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Rounded.Delete, "Delete", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: PlaylistWithSongCount,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AccentPurple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.QueueMusic, null, tint = AccentPurple, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${playlist.songCount} songs",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Rounded.Delete, "Delete", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
        }
    }
}
