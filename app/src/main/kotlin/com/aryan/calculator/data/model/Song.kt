package com.aryan.calculator.data.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val duration: Int = 0,
    val artwork: String = "",
    val streamUrl: String,
    val downloaded: Boolean = false,
    val isLiked: Boolean = false
)
