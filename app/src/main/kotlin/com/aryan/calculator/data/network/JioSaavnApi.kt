package com.aryan.calculator.data.network

import android.util.Base64
import com.aryan.calculator.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * JioSaavn's internal web API, ported from the desktop app's src/jiosaavn.js.
 * Media URLs are DES-ECB encrypted with a fixed public key ("38346591") --
 * Android's crypto provider supports DES-ECB natively, unlike Node/OpenSSL3.
 */
object JioSaavnApi {

    private const val BASE = "https://www.jiosaavn.com/api.php"
    private const val COMMON = "&_format=json&_marker=0&api_version=4&ctx=web6dot0"
    private const val DES_KEY = "38346591"

    private val client = OkHttpClient()

    private fun encode(s: String): String = URLEncoder.encode(s, "UTF-8")

    private fun decodeEntities(str: String?): String {
        if (str.isNullOrEmpty()) return ""
        return str.replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&apos;", "'")
    }

    private fun upgradeImg(url: String?): String {
        if (url.isNullOrEmpty()) return ""
        return url.replaceFirst(Regex("^http:"), "https:")
            .replace("150x150", "500x500")
            .replace("50x50", "500x500")
    }

    private fun buildStreamUrl(encrypted: String, has320: Boolean): String? {
        return try {
            val keySpec = SecretKeySpec(DES_KEY.toByteArray(Charsets.UTF_8), "DES")
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decoded = Base64.decode(encrypted, Base64.DEFAULT)
            val decrypted = String(cipher.doFinal(decoded), Charsets.UTF_8)
            if (decrypted.isBlank()) return null
            val https = decrypted.replaceFirst(Regex("^http:"), "https:")
            val quality = if (has320) "_320" else "_160"
            https.replace("_96.mp4", "${quality}.mp4")
        } catch (e: Exception) {
            null
        }
    }

    private fun mapSong(s: JSONObject): Song? {
        val info = s.optJSONObject("more_info") ?: JSONObject()

        val primaryArtists = info.optJSONObject("artistMap")?.optJSONArray("primary_artists")
        val fromMap = primaryArtists?.takeIf { it.length() > 0 }?.let { arr ->
            (0 until arr.length()).joinToString(", ") { i -> arr.optJSONObject(i)?.optString("name") ?: "" }
        }?.takeIf { it.isNotBlank() }
        val artists = fromMap
            ?: info.optString("music").takeIf { it.isNotBlank() }
            ?: decodeEntities(info.optString("subtitle")).takeIf { it.isNotBlank() }
            ?: "Unknown artist"

        val has320raw = info.opt("320kbps")
        val has320 = has320raw == "true" || has320raw == true
        val encrypted = info.optString("encrypted_media_url").takeIf { it.isNotBlank() } ?: return null
        val streamUrl = buildStreamUrl(encrypted, has320) ?: return null

        val id = s.optString("id").takeIf { it.isNotBlank() } ?: return null
        val rawTitle = s.optString("title").ifBlank { s.optString("song") }

        return Song(
            id = id,
            title = decodeEntities(rawTitle),
            artist = decodeEntities(artists),
            album = decodeEntities(info.optString("album")),
            duration = info.optString("duration").toIntOrNull() ?: 0,
            artwork = upgradeImg(s.optString("image")),
            streamUrl = streamUrl
        )
    }

    private suspend fun getText(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .header("Accept", "application/json")
            .build()
        client.newCall(request).execute().use { it.body?.string() ?: "" }
    }

    suspend fun searchSongs(query: String, limit: Int = 40): List<Song> {
        val url = "$BASE?__call=search.getResults&q=${encode(query)}&n=$limit$COMMON"
        return try {
            val json = JSONObject(getText(url))
            val results = json.optJSONArray("results") ?: JSONArray()
            (0 until results.length()).mapNotNull { mapSong(results.optJSONObject(it) ?: JSONObject()) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAlbumSongs(albumId: String): List<Song> {
        val url = "$BASE?__call=content.getAlbumDetails&albumid=${encode(albumId)}$COMMON"
        return try {
            val json = JSONObject(getText(url))
            val list = json.optJSONArray("songs") ?: json.optJSONArray("list") ?: JSONArray()
            (0 until list.length()).mapNotNull { mapSong(list.optJSONObject(it) ?: JSONObject()) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun norm(s: String?): String = (s ?: "").lowercase().replace(Regex("[^a-z0-9]"), "")

    private data class AlbumMatch(val id: String, val title: String)

    // Prefix match only when the two titles are of comparable length, so a short
    // album like "295" does NOT hijack a longer query like "295 Sidhu Moose Wala"
    // (which names an artist, not that album).
    private suspend fun findAlbum(query: String): AlbumMatch? {
        val url = "$BASE?__call=search.getAlbumResults&q=${encode(query)}&n=4$COMMON"
        return try {
            val json = JSONObject(getText(url))
            val albums = json.optJSONArray("results") ?: JSONArray()
            val nq = norm(query)
            if (nq.isBlank()) return null
            for (i in 0 until albums.length()) {
                val a = albums.optJSONObject(i) ?: continue
                val nt = norm(a.optString("title"))
                if (nt.isBlank()) continue
                val id = a.optString("id")
                if (nt == nq) return AlbumMatch(id, a.optString("title"))
                val shorter = minOf(nt.length, nq.length)
                val longer = maxOf(nt.length, nq.length)
                if ((nt.startsWith(nq) || nq.startsWith(nt)) && longer > 0 && shorter.toDouble() / longer >= 0.6) {
                    return AlbumMatch(id, a.optString("title"))
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /** Smart search: a movie/album query surfaces its full soundtrack first, then matching songs. */
    suspend fun search(query: String, limit: Int = 40): List<Song> = coroutineScope {
        val songsDeferred = async { searchSongs(query, limit) }
        val albumDeferred = async { findAlbum(query) }
        val songs = songsDeferred.await()
        val album = albumDeferred.await()
        val albumSongs = if (album != null) getAlbumSongs(album.id) else emptyList()

        val seen = HashSet<String>()
        val merged = ArrayList<Song>()
        for (t in albumSongs + songs) {
            if (seen.add(t.id)) merged.add(t)
        }
        merged
    }

    suspend fun home(languages: Set<String> = setOf("hindi")): List<Song> = coroutineScope {
        val trendingQueries = listOf(
            "latest bollywood hits 2024",
            "arijit singh new songs",
            "top hindi songs 2024",
            "trending punjabi songs 2024",
            "new romantic hindi songs",
            "bollywood party songs",
            "atif aslam hits",
            "jubin nautiyal songs",
            "diljit dosanjh",
            "ap dhillon"
        )

        val deferredList = trendingQueries.map { query ->
            async {
                try {
                    searchSongs(query, 10)
                } catch (e: Exception) {
                    emptyList<Song>()
                }
            }
        }

        val allSongs = deferredList.flatMap { it.await() }

        val seen = HashSet<String>()
        val merged = ArrayList<Song>()
        for (song in allSongs) {
            if (song.duration >= 120 && seen.add(song.id)) merged.add(song)
        }
        merged.take(60)
    }

    private suspend fun getCharts(language: String): List<Song> {
        val url = "$BASE?__call=content.getCharts&type=song&language=$language$COMMON"
        return try {
            val text = getText(url).trim()
            val json = if (text.startsWith("[")) JSONObject().put("data", JSONArray(text)) else JSONObject(text)
            val list = json.optJSONArray("data") ?: json.optJSONArray("results") ?: JSONArray()
            (0 until list.length()).mapNotNull { mapSong(list.optJSONObject(it) ?: JSONObject()) }
        } catch (e: Exception) {
            searchSongs("$language songs 2024", 20)
        }
    }
}
