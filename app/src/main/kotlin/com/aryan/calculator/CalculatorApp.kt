package com.aryan.calculator

import android.app.Application
import com.aryan.calculator.data.DownloadManager
import com.aryan.calculator.data.MusicRepository
import com.aryan.calculator.data.DownloadNotificationHelper
import com.aryan.calculator.data.UpdateNotificationHelper
import com.aryan.calculator.data.local.AppDatabase
import com.aryan.calculator.data.local.UserPreferences
import com.aryan.calculator.playback.PlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CalculatorApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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
            recentlyPlayedDao = db.recentlyPlayedDao(),
            playlistDao = db.playlistDao()
        )
        playerController = PlayerController(this)
        userPreferences = UserPreferences(this)

        // Create notification channels and check for updates
        UpdateNotificationHelper.createNotificationChannel(this)
        DownloadNotificationHelper.createNotificationChannel(this)
        applicationScope.launch {
            UpdateNotificationHelper.checkAndNotify(this@CalculatorApp)
        }
    }
}
