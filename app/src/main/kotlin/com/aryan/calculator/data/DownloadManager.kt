package com.aryan.calculator.data

import android.content.Context
import android.net.Uri
import com.aryan.calculator.data.local.DownloadedSongDao
import com.aryan.calculator.data.local.DownloadedSongEntity
import com.aryan.calculator.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/** Offline downloads, ported from the desktop app's src/downloads.js -- audio + artwork
 *  saved to app-scoped external storage, indexed in Room instead of library.json. */
class DownloadManager(context: Context, private val dao: DownloadedSongDao) {

    private val client = OkHttpClient()
    private val dir: File = File(context.getExternalFilesDir(null), "Calculator").apply { mkdirs() }

    fun observeDownloads(): Flow<List<Song>> =
        dao.observeAll().map { list -> list.map { it.toSong() } }

    suspend fun isDownloaded(id: String): Boolean = dao.findById(id) != null

    suspend fun downloadedIds(): Set<String> = dao.getAllIds().toSet()

    suspend fun download(song: Song): Song = withContext(Dispatchers.IO) {
        val audioFile = File(dir, "${song.id}.m4a")
        val artFile = File(dir, "${song.id}.jpg")

        fetchToFile(song.streamUrl, audioFile)
        if (song.artwork.isNotBlank()) {
            runCatching { fetchToFile(song.artwork, artFile) }
        }

        val entity = DownloadedSongEntity(
            id = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album,
            duration = song.duration,
            artworkPath = if (artFile.exists()) artFile.absolutePath else "",
            audioPath = audioFile.absolutePath,
            downloadedAt = System.currentTimeMillis()
        )
        dao.insert(entity)
        entity.toSong()
    }

    suspend fun remove(id: String) = withContext(Dispatchers.IO) {
        val entity = dao.findById(id)
        if (entity != null) {
            File(entity.audioPath).delete()
            if (entity.artworkPath.isNotBlank()) File(entity.artworkPath).delete()
            dao.deleteById(id)
        }
    }

    private fun fetchToFile(url: String, target: File) {
        val request = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("Download failed: ${resp.code}")
            val body = resp.body ?: throw IOException("Empty response body")
            body.byteStream().use { input ->
                FileOutputStream(target).use { output -> input.copyTo(output) }
            }
        }
    }

    private fun DownloadedSongEntity.toSong() = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        artwork = if (artworkPath.isNotBlank()) Uri.fromFile(File(artworkPath)).toString() else "",
        streamUrl = Uri.fromFile(File(audioPath)).toString(),
        downloaded = true
    )
}
