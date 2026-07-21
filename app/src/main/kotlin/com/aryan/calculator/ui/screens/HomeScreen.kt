package com.aryan.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aryan.calculator.data.model.Song
import com.aryan.calculator.ui.components.SongCard
import com.aryan.calculator.ui.theme.AccentPink
import com.aryan.calculator.ui.theme.GradientPink
import com.aryan.calculator.ui.theme.GradientPurple
import java.util.Calendar

@Composable
fun HomeScreen(
    songs: List<Song>,
    isLoading: Boolean,
    onSongClick: (Song) -> Unit,
    onDownloadToggle: (Song) -> Unit,
    currentPlayingSongId: String? = null,
    recentlyPlayed: List<Song> = emptyList(),
    onDownloadClick: (Song) -> Unit = {}
) {
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..20 -> "Good Evening"
        else -> "Good Night"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = "Swardhun",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    ),
                    color = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GradientPink, GradientPurple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        if (isLoading && songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentPink)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading music...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                if (recentlyPlayed.isNotEmpty()) {
                    item {
                        SectionHeader("Recently Played")
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recentlyPlayed.take(10), key = { "recent_${it.id}" }) { song ->
                                RecentlyPlayedCard(
                                    song = song,
                                    onClick = { onSongClick(song) },
                                    isPlaying = song.id == currentPlayingSongId
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                item {
                    SectionHeader("Trending Now")
                }

                items(songs.take(6), key = { "trending_${it.id}" }) { song ->
                    SongCard(
                        song = song,
                        onClick = { onSongClick(song) },
                        onDownloadToggle = { onDownloadToggle(song) },
                        isPlaying = song.id == currentPlayingSongId,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        onOptionsClick = { onDownloadToggle(song) },
                        onDownloadClick = { onDownloadClick(song) }
                    )
                }

                if (songs.size > 6) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        SectionHeader("Explore More")
                    }

                    items(songs.drop(6), key = { it.id }) { song ->
                        SongCard(
                            song = song,
                            onClick = { onSongClick(song) },
                            onDownloadToggle = { onDownloadToggle(song) },
                            isPlaying = song.id == currentPlayingSongId,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            onOptionsClick = { onDownloadToggle(song) },
                            onDownloadClick = { onDownloadClick(song) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        ),
        color = Color.White,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun RecentlyPlayedCard(
    song: Song,
    onClick: () -> Unit,
    isPlaying: Boolean
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            AsyncImage(
                model = song.artwork,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(130.dp)
                    .shadow(12.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AccentPink),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("▶", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = if (isPlaying) AccentPink else Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
