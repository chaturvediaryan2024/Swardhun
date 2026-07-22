package com.aryan.calculator.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.imageLoader
import com.aryan.calculator.ui.theme.*

private val ALL_LANGUAGES = listOf(
    "hindi", "english", "punjabi", "tamil", "telugu",
    "kannada", "bhojpuri", "marathi", "gujarati", "bengali", "haryanvi", "urdu"
)

@Composable
fun SettingsScreen(
    audioQuality: String,
    onAudioQualityChange: (String) -> Unit,
    selectedLanguages: Set<String>,
    onLanguagesChange: (Set<String>) -> Unit,
    downloadCount: Int,
    onClearDownloads: () -> Unit,
    appVersion: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scroll = rememberScrollState()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showClearCacheMsg by remember { mutableStateOf(false) }
    var showClearDownloadsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, "Back", tint = Color.White)
            }
            Text(
                "Settings",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(bottom = 120.dp)
        ) {
            SectionLabel("Audio")

            SettingRow(
                icon = Icons.Rounded.HighQuality,
                title = "Audio Quality",
                subtitle = if (audioQuality == "high") "High (320 kbps)" else "Normal (160 kbps)"
            ) {
                // Toggle segmented
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QualityChip("Normal", audioQuality == "normal") { onAudioQualityChange("normal") }
                    QualityChip("High", audioQuality == "high") { onAudioQualityChange("high") }
                }
            }

            SettingRow(
                icon = Icons.Rounded.Language,
                title = "Music Languages",
                subtitle = selectedLanguages.joinToString(", ") { it.replaceFirstChar { c -> c.uppercase() } }
                    .ifBlank { "None selected" },
                onClick = { showLanguageDialog = true }
            )

            SectionLabel("Storage")

            SettingRow(
                icon = Icons.Rounded.CleaningServices,
                title = "Clear Image Cache",
                subtitle = "Free up space used by artwork",
                onClick = {
                    context.imageLoader.memoryCache?.clear()
                    context.imageLoader.diskCache?.clear()
                    showClearCacheMsg = true
                }
            )

            SettingRow(
                icon = Icons.Rounded.Download,
                title = "Downloaded Songs",
                subtitle = "$downloadCount songs downloaded",
                onClick = { if (downloadCount > 0) showClearDownloadsDialog = true }
            )

            SectionLabel("About")

            SettingRow(
                icon = Icons.Rounded.Info,
                title = "Version",
                subtitle = "Musify v$appVersion"
            )

            SettingRow(
                icon = Icons.Rounded.Feedback,
                title = "Send Feedback",
                subtitle = "Report a bug or suggest a feature",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("feedback@musify.app"))
                        putExtra(Intent.EXTRA_SUBJECT, "Musify Feedback (v$appVersion)")
                    }
                    runCatching { context.startActivity(Intent.createChooser(intent, "Send feedback")) }
                }
            )

            SettingRow(
                icon = Icons.Rounded.Favorite,
                title = "Developed by Aaru",
                subtitle = "Made with ❤️ • 2026"
            )
        }
    }

    if (showLanguageDialog) {
        var temp by remember { mutableStateOf(selectedLanguages) }
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = GlassBg,
            title = { Text("Music Languages", color = Color.White) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ALL_LANGUAGES.forEach { lang ->
                        val checked = temp.contains(lang)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    temp = if (checked) temp - lang else temp + lang
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { temp = if (checked) temp - lang else temp + lang },
                                colors = CheckboxDefaults.colors(checkedColor = AccentPurple)
                            )
                            Text(
                                lang.replaceFirstChar { it.uppercase() },
                                color = Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (temp.isNotEmpty()) onLanguagesChange(temp)
                    showLanguageDialog = false
                }) { Text("Save", color = AccentPurple) }
            },
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }

    if (showClearCacheMsg) {
        AlertDialog(
            onDismissRequest = { showClearCacheMsg = false },
            containerColor = GlassBg,
            title = { Text("Cache Cleared", color = Color.White) },
            text = { Text("Image cache has been cleared.", color = Color.White.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = { showClearCacheMsg = false }) { Text("OK", color = AccentPurple) }
            }
        )
    }

    if (showClearDownloadsDialog) {
        AlertDialog(
            onDismissRequest = { showClearDownloadsDialog = false },
            containerColor = GlassBg,
            title = { Text("Remove All Downloads?", color = Color.White) },
            text = {
                Text(
                    "All $downloadCount downloaded songs will be removed from this device.",
                    color = Color.White.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onClearDownloads()
                    showClearDownloadsDialog = false
                }) { Text("Remove All", color = HeartRed) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDownloadsDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = AccentPurple,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                Icons.Rounded.ChevronRight,
                null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun QualityChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) AccentPurple else Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
