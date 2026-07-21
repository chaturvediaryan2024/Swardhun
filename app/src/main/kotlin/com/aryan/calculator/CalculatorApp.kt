package com.aryan.calculator

import android.app.Application
import com.aryan.calculator.data.DownloadManager
import com.aryan.calculator.data.MusicRepository
import com.aryan.calculator.data.local.AppDatabase
import com.aryan.calculator.data.local.UserPreferences
import com.aryan.calculator.playback.PlayerController

class CalculatorApp : Application() {

    lateinit var repository: MusicRepository
        private set
    lateinit var playerController: PlayerController
        private set
    lateinit var userPreferences: UserPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        val downloadManager = DownloadManager(this, db.downloadedSongDao())
        repository = MusicRepository(
            downloadManager = downloadManager,
            likedSongDao = db.likedSongDao(),
            recentlyPlayedDao = db.recentlyPlayedDao()
        )
        playerController = PlayerController(this)
        userPreferences = UserPreferences(this)
    }
}
