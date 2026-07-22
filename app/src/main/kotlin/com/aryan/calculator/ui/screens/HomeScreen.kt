package com.aryan.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aryan.calculator.data.model.Song
import com.aryan.calculator.ui.theme.AccentLime
import com.aryan.calculator.ui.theme.BgDark
import com.aryan.calculator.ui.theme.GlassBg

private val homeChips = listOf("All", "Trending", "New Release", "Top")

@Composable
fun HomeScreen(
    songs: List<Song>,
    isLoading: Boolean,
    onSongClick: (Song) -> Unit,
    onOptionsClick: (Song) -> Unit,
    onLike: (Song) -> Unit = {},
    likedIds: Set<String> = emptySet(),
    currentPlayingSongId: String? = null,
    recentlyPlayed: List<Song> = emptyList(),
    userName: String = "",
    userPhotoUri: String = "",
    onProfileClick: () -> Unit = {},
    categorySongs: Map<Int, List<Song>> = emptyMap(),
    loadingCategory: Int? = null,
    onChipSelected: (Int) -> Unit = {}
) {
    var selectedChip by remember { mutableStateOf(0) }

    val isAll = selectedChip == 0
    val visibleSongs = if (isAll) songs.take(20) else (categorySongs[selectedChip] ?: emptyList())
    val chipLoading = !isAll && loadingCategory == selectedChip && visibleSongs.isEmpty()

    // Discover Weekly rotates through the catalogue every few seconds.
    var featuredIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(songs.size) {
        if (songs.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(7000)
            featuredIndex = (featuredIndex + 1) % songs.size
        }
    }
    val featured = songs.getOrNull(featuredIndex) ?: songs.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        if (isLoading && songs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentLime)
                    Spacer(Modifier.height(16.dp))
                    Text("Loading music...", color = Color.White.copy(alpha = 0.6f))
                }
            }
            return@Box
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
            // Header
            item {
                HomeHeader(
                    userName = userName,
                    userPhotoUri = userPhotoUri,
                    onProfileClick = onProfileClick
                )
            }

            // Filter chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(homeChips) { index, label ->
                        ChipPill(
                            label = label,
                            selected = index == selectedChip,
                            onClick = {
                                selectedChip = index
                                onChipSelected(index)
                            }
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Discover + Recently played only on the "All" tab.
            if (isAll && featured != null) {
                item {
                    SectionTitle("Curated & trending")
                    DiscoverCard(
                        song = featured,
                        onPlay = { onSongClick(featured) },
                        onClick = { onSongClick(featured) },
                        onLike = { onLike(featured) },
                        onOptions = { onOptionsClick(featured) }
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }

            if (isAll && recentlyPlayed.isNotEmpty()) {
                item {
                    SectionTitle("Recently Played")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(recentlyPlayed.take(10), key = { "recent_${it.id}" }) { song ->
                            RecentCard(
                                song = song,
                                isPlaying = song.id == currentPlayingSongId,
                                onClick = { onSongClick(song) }
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            // Section title for the list
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAll) "Top daily hits" else homeChips[selectedChip],
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "${visibleSongs.size} songs",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            if (chipLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentLime)
                    }
                }
            } else if (!isAll && visibleSongs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No songs found", color = Color.White.copy(alpha = 0.5f))
                    }
                }
            } else {
                itemsIndexed(visibleSongs, key = { _, s -> "list_${s.id}" }) { index, song ->
                    DailySongRow(
                        rank = index + 1,
                        song = song,
                        isPlaying = song.id == currentPlayingSongId,
                        onClick = { onSongClick(song) },
                        onOptions = { onOptionsClick(song) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    userName: String,
    userPhotoUri: String,
    onProfileClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GlassBg)
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                if (userPhotoUri.isNotEmpty()) {
                    AsyncImage(
                        model = userPhotoUri,
                        contentDescription = "Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                    )
                } else {
                    Icon(Icons.Rounded.Person, null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = "Hi, ${userName.ifBlank { "there" }}",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            ),
            color = Color.White,
            fontSize = 30.sp
        )
        Text(
            text = "What do you want to hear today?",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun CircleIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(GlassBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ChipPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) AccentLime else GlassBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color.Black else Color.White.copy(alpha = 0.8f),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = Color.White,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun DiscoverCard(
    song: Song,
    onPlay: () -> Unit,
    onClick: () -> Unit,
    onLike: () -> Unit,
    onOptions: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(170.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF6D5BD0), Color(0xFF3B2A78))
                )
            )
            .clickable(onClick = onClick)
    ) {
        // Artwork on the right (crossfades as the featured song rotates)
        AsyncImage(
            model = song.artwork,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp)
                .rotate(6f)
                .size(120.dp)
                .clip(RoundedCornerShape(14.dp))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.62f)) {
                Text(
                    text = "Discover Weekly",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AccentLime)
                        .clickable(onClick = onPlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.PlayArrow, "Play", tint = Color.Black, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(16.dp))
                Icon(
                    Icons.Rounded.FavoriteBorder,
                    "Like",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(26.dp).clip(CircleShape).clickable(onClick = onLike)
                )
                Spacer(Modifier.width(18.dp))
                Icon(
                    Icons.Rounded.MoreHoriz, "More",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(26.dp).clip(CircleShape).clickable(onClick = onOptions)
                )
            }
        }
    }
}

@Composable
private fun RecentCard(song: Song, isPlaying: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(140.dp).clickable(onClick = onClick)
    ) {
        Box {
            AsyncImage(
                model = song.artwork,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassBg)
            )
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (isPlaying) AccentLime else Color.White,
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
}

@Composable
private fun DailySongRow(
    rank: Int,
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onOptions: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.artwork,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GlassBg)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = if (isPlaying) AccentLime else Color.White,
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isPlaying) AccentLime else GlassBg)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.PlayArrow,
                "Play",
                tint = if (isPlaying) Color.Black else Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
