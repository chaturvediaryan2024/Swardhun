package com.aryan.calculator.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddToQueue
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aryan.calculator.data.model.Song
import com.aryan.calculator.ui.theme.AccentPink
import com.aryan.calculator.ui.theme.AccentTeal
import com.aryan.calculator.ui.theme.GlassBg
import com.aryan.calculator.ui.theme.HeartRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsSheet(
    song: Song,
    isLiked: Boolean,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onLike: () -> Unit,
    onAddToQueue: () -> Unit,
    onShare: () -> Unit,
    onViewArtist: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GlassBg,
        dragHandle = {
            Spacer(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = song.artwork,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OptionItem(
                icon = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                label = if (isLiked) "Remove from Liked" else "Add to Liked",
                tint = if (isLiked) HeartRed else Color.White,
                onClick = {
                    onLike()
                    onDismiss()
                }
            )

            OptionItem(
                icon = if (song.downloaded) Icons.Rounded.DownloadDone else Icons.Rounded.Download,
                label = if (song.downloaded) "Downloaded" else "Download",
                tint = if (song.downloaded) AccentTeal else Color.White,
                onClick = {
                    if (!song.downloaded) {
                        onDownload()
                    }
                    onDismiss()
                }
            )

            OptionItem(
                icon = Icons.Rounded.AddToQueue,
                label = "Add to Queue",
                onClick = {
                    onAddToQueue()
                    onDismiss()
                }
            )

            OptionItem(
                icon = Icons.Rounded.Share,
                label = "Share",
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Listen to ${song.title} by ${song.artist} on Swardhun!")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    onDismiss()
                }
            )

            OptionItem(
                icon = Icons.Rounded.Person,
                label = "View Artist: ${song.artist.split(",").first().trim()}",
                onClick = {
                    onViewArtist()
                    onDismiss()
                }
            )

            if (song.album.isNotBlank()) {
                OptionItem(
                    icon = Icons.Rounded.Album,
                    label = "View Album: ${song.album}",
                    onClick = {
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun OptionItem(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}
