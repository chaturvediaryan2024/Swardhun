package com.aryan.calculator.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aryan.calculator.data.AppUpdate
import com.aryan.calculator.data.UpdateChecker
import com.aryan.calculator.data.local.UserProfile
import com.aryan.calculator.ui.theme.*

private data class QuickAccess(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color,
    val onClick: () -> Unit
)

@Composable
fun ProfileScreen(
    profile: UserProfile,
    likedCount: Int,
    downloadCount: Int,
    playedCount: Int,
    listeningMinutes: Int,
    onNameChange: (String) -> Unit,
    onPhotoChange: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLiked: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onShareApp: () -> Unit = {}
) {
    var showNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(profile.name) }
    var availableUpdate by remember { mutableStateOf<AppUpdate?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) { availableUpdate = UpdateChecker.checkForUpdate() }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { onPhotoChange(it.toString()) } }

    val quickAccess = listOf(
        QuickAccess("Liked Songs", "$likedCount songs", Icons.Rounded.Favorite, HeartRed, onOpenLiked),
        QuickAccess("Downloads", "$downloadCount offline", Icons.Rounded.Download, AccentTeal, onOpenDownloads),
        QuickAccess("History", "Recently played", Icons.Rounded.History, AccentBlue, onOpenHistory),
        QuickAccess("Queue", "Up next", Icons.Rounded.QueueMusic, AccentPurple, onOpenQueue),
        QuickAccess("Equalizer", "Tune your sound", Icons.Rounded.Equalizer, AccentOrange, onOpenEqualizer),
        QuickAccess("Share App", "Invite your friends", Icons.Rounded.Share, AccentPink, onShareApp),
        QuickAccess("Settings", "App preferences", Icons.Rounded.Settings, AccentLime, onOpenSettings)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BgDark),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // ---- Header with premium gradient ----
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                GradientPurple.copy(alpha = 0.7f),
                                AccentBlue.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(top = 24.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        if (profile.photoUri.isNotEmpty()) {
                            AsyncImage(
                                model = profile.photoUri,
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, AccentLime.copy(alpha = 0.6f), CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(GradientPink, GradientPurple)))
                                    .border(3.dp, AccentLime.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Person, null, tint = Color.White, modifier = Modifier.size(54.dp))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(AccentLime)
                                .clickable { photoPicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.CameraAlt, "Change photo", tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = profile.name.ifEmpty { "Set Your Name" },
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (profile.name.isEmpty()) Color.White.copy(alpha = 0.6f) else Color.White
                    )
                    Text("Music Lover", style = MaterialTheme.typography.bodyMedium, color = AccentLime)

                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { tempName = profile.name; showNameDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Rounded.Edit, null, Modifier.size(18.dp), tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Profile", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // ---- Update banner (if available) ----
        availableUpdate?.let { update ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentLime.copy(alpha = 0.15f))
                        .border(1.dp, AccentLime.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { showUpdateDialog = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.SystemUpdate, null, tint = AccentLime, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Update available", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Version ${update.versionName} is ready", color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Rounded.ChevronRight, null, tint = Color.White.copy(alpha = 0.5f))
                }
            }
        }

        // ---- Listening statistics ----
        item {
            SectionLabel("Your Stats")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile("Played", playedCount.toString(), Icons.Rounded.MusicNote, AccentBlue, Modifier.weight(1f))
                StatTile("Liked", likedCount.toString(), Icons.Rounded.Favorite, HeartRed, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile("Downloads", downloadCount.toString(), Icons.Rounded.Download, AccentTeal, Modifier.weight(1f))
                StatTile(
                    "Listening",
                    if (listeningMinutes >= 60) "${listeningMinutes / 60}h ${listeningMinutes % 60}m" else "${listeningMinutes}m",
                    Icons.Rounded.Headphones, AccentOrange, Modifier.weight(1f)
                )
            }
        }

        // ---- Quick access ----
        item { SectionLabel("Your Library") }
        items(quickAccess.chunked(2).size) { rowIndex ->
            val rowItems = quickAccess.chunked(2)[rowIndex]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { qa ->
                    QuickAccessCard(qa, Modifier.weight(1f))
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = GlassBg,
            title = { Text("Your Name", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    placeholder = { Text("Enter your name", color = Color.White.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        cursorColor = AccentLime, focusedBorderColor = AccentLime,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { onNameChange(tempName); showNameDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentLime)
                ) { Text("Save", color = Color.Black) }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }

    if (showUpdateDialog && availableUpdate != null) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            containerColor = GlassBg,
            title = { Text("New Update", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Version ${availableUpdate!!.versionName}", color = AccentLime, fontWeight = FontWeight.Bold)
                    if (availableUpdate!!.releaseNotes.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(availableUpdate!!.releaseNotes.take(400), color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { UpdateChecker.openDownloadPage(context, availableUpdate!!.downloadUrl); showUpdateDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentLime)
                ) { Text("Download", color = Color.Black, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Later", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = Color.White,
        modifier = Modifier.padding(start = 20.dp, top = 22.dp, bottom = 12.dp)
    )
}

@Composable
private fun StatTile(label: String, value: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassBg)
            .padding(16.dp)
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(10.dp))
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
    }
}

@Composable
private fun QuickAccessCard(qa: QuickAccess, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassBg)
            .clickable(onClick = qa.onClick)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(qa.tint.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(qa.icon, null, tint = qa.tint, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(qa.label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
        Text(qa.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
    }
}
