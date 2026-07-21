package com.aryan.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryan.calculator.data.model.Song
import com.aryan.calculator.ui.components.SongCard
import com.aryan.calculator.ui.theme.AccentPink
import com.aryan.calculator.ui.theme.GlassBg

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
            query.isBlank() -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Color.White.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Find your music",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Search by song, artist or movie",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
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
