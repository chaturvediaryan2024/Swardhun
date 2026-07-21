package com.aryan.calculator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Int,
    val artwork: String,
    val streamUrl: String,
    val playedAt: Long = System.currentTimeMillis()
)
