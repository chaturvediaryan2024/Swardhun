package com.aryan.calculator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_songs")
data class DownloadedSongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Int,
    val artworkPath: String,
    val audioPath: String,
    val downloadedAt: Long
)
