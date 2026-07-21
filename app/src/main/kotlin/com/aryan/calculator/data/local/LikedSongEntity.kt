package com.aryan.calculator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "liked_songs")
data class LikedSongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Int,
    val artwork: String,
    val streamUrl: String,
    val likedAt: Long = System.currentTimeMillis()
)
