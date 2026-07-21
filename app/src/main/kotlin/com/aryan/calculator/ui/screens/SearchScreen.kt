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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryan.calculator.data.model.Song
import com.aryan.calculator.ui.components.SongCard
import com.aryan.calculator.ui.theme.AccentPink
import com.aryan.calculator.ui.theme.AccentTeal
import com.aryan.calculator.ui.theme.GlassBg

private data class TrendingItem(val name: String, val type: String)
private data class CategoryItem(val name: String, val color1: Color, val color2: Color)

private val trendingItems = listOf(
    TrendingItem("Arijit Singh", "Artist"),
    TrendingItem("Pushpa 2", "Album"),
    TrendingItem("Diljit Dosanjh", "Artist"),
    TrendingItem("Animal", "Album"),
    TrendingItem("AP Dhillon", "Artist"),
    TrendingItem("Atif Aslam", "Artist")
)

private val categories = listOf(
    CategoryItem("Romance", Color(0xFFE91E63), Color(0xFFC2185B)),
    CategoryItem("Party", Color(0xFF9C27B0), Color(0xFF7B1FA2)),
    CategoryItem("Bollywood", Color(0xFFFF5722), Color(0xFFE64A19)),
    CategoryItem("Punjabi", Color(0xFF00BCD4), Color(0xFF0097A7)),
    CategoryItem("Workout", Color(0xFF4CAF50), Color(0xFF388E3C)),
    CategoryItem("Chill", Color(0xFF3F51B5), Color(0xFF303F9F)),
    CategoryItem("Devotional", Color(0xFFFF9800), Color(0xFFF57C00)),
    CategoryItem("90s Hits", Color(0xFF795548), Color(0xFF5D4037))
)

@Composable
fun SearchScreen(
    query: String,
    results: List<Song>,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    onDownloadToggle: (Song) -> Unit,
    currentPlayingSongId: String? = null,
    onDownloadClick: (Song) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            ),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp)),
            placeholder = {
                Text(
                    "Artists, songs, movies...",
                    color = Color.White.copy(alpha = 0.4f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Clear",
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GlassBg,
                unfocusedContainerColor = GlassBg,
                disabledContainerColor = GlassBg,
                cursorColor = AccentPink,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isSearching -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentPink, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Searching...",
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            query.isBlank() -> SearchIdleContent(onQueryChange = onQueryChange)
            results.isEmpty() -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No results",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try different keywords",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    Text(
                        text = "${results.size} results",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
                items(results, key = { it.id }) { song ->
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

@Composable
private fun SearchIdleContent(onQueryChange: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Text(
                text = "Trending",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trendingItems) { item ->
                    TrendingCard(
                        item = item,
                        onClick = { onQueryChange(item.name) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Browse All",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(categories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onQueryChange("${category.name} songs") }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendingCard(item: TrendingItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(AccentPink.copy(alpha = 0.3f), AccentTeal.copy(alpha = 0.3f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.name.take(2).uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = item.type,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun CategoryCard(category: CategoryItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(category.color1, category.color2)
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}
