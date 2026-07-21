package com.aryan.calculator.data

import com.aryan.calculator.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UpdateNotificationHelper {

    private const val CHANNEL_ID = "update_channel"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "App Updates"
            val descriptionText = "Notifications for app updates"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun checkAndNotify(context: Context) = withContext(Dispatchers.IO) {
        try {
            val update = UpdateChecker.checkForUpdate()
            if (update != null) {
                showUpdateNotification(context, update)
            }
        } catch (e: Exception) {
            // Silent fail
        }
    }

    private fun showUpdateNotification(context: Context, update: AppUpdate) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(update.downloadUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🎵 Musify Update Available!")
            .setContentText("Version ${update.versionName} is ready to download")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("A new version of Musify (v${update.versionName}) is available. Tap to download and enjoy new features!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_notification,
                "Download Now",
                pendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
