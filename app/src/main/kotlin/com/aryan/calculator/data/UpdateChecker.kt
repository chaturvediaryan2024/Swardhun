package com.aryan.calculator.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class AppUpdate(
    val versionName: String,
    val versionCode: Int,
    val downloadUrl: String,
    val releaseNotes: String
)

object UpdateChecker {

    private const val GITHUB_API_URL = "https://api.github.com/repos/chaturvediaryan2024/Swardhun/releases/latest"
    private const val CURRENT_VERSION_CODE = 37

    private val client = OkHttpClient()

    suspend fun checkForUpdate(): AppUpdate? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(GITHUB_API_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val json = JSONObject(response.body?.string() ?: return@withContext null)
                val tagName = json.optString("tag_name", "").removePrefix("v")
                val versionCode = parseVersionCode(tagName)
                val releaseNotes = json.optString("body", "")

                val assets = json.optJSONArray("assets")
                var downloadUrl = ""
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.optJSONObject(i)
                        val name = asset?.optString("name", "") ?: ""
                        if (name.endsWith(".apk")) {
                            downloadUrl = asset?.optString("browser_download_url", "") ?: ""
                            break
                        }
                    }
                }

                if (downloadUrl.isEmpty()) {
                    downloadUrl = json.optString("html_url", "")
                }

                if (versionCode > CURRENT_VERSION_CODE) {
                    AppUpdate(
                        versionName = tagName,
                        versionCode = versionCode,
                        downloadUrl = downloadUrl,
                        releaseNotes = releaseNotes
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseVersionCode(version: String): Int {
        return try {
            val parts = version.split(".")
            when (parts.size) {
                1 -> parts[0].toInt()
                2 -> parts[0].toInt() * 10 + parts[1].toInt()
                else -> parts[0].toInt() * 100 + parts[1].toInt() * 10 + parts[2].toInt()
            }
        } catch (e: Exception) {
            0
        }
    }

    fun openDownloadPage(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}
