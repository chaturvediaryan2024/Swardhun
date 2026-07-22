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

    /**
     * Re-resolve a fresh, playable stream URL for a song by its id.
     * JioSaavn stream URLs are time-limited, so a song saved in a playlist / liked /
     * recently-played days ago will have a dead URL -- this fetches a live one.
     */
    suspend fun getSongById(id: String): Song? {
        val url = "$BASE?__call=song.getDetails&pids=${encode(id)}$COMMON"
        return try {
            val json = JSONObject(getText(url))
            // api_version 4 -> { "songs": [ {...} ] }
            json.optJSONArray("songs")?.takeIf { it.length() > 0 }?.let {
                return mapSong(it.optJSONObject(0) ?: return null)
            }
            // older shape -> { "<id>": {...} }
            json.optJSONObject(id)?.let { return mapSong(it) }
            // fallback: first object that looks like a song
            val keys = json.keys()
            while (keys.hasNext()) {
                val obj = json.optJSONObject(keys.next())
                if (obj != null && obj.has("more_info")) return mapSong(obj)
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /** Real image (photo) for an artist/entity by name -- used by the search "Trending" row. */
    suspend fun getEntityImage(name: String): String? {
        // Artist search first (gives the artist's real photo).
        val artistUrl = "$BASE?__call=search.getArtistResults&q=${encode(name)}&n=1$COMMON"
        runCatching {
            val json = JSONObject(getText(artistUrl))
            val results = json.optJSONArray("results") ?: JSONArray()
            val img = results.optJSONObject(0)?.optString("image")
            if (!img.isNullOrBlank()) return upgradeImg(img)
        }
        // Fallback: first matching song's artwork.
        return runCatching { searchSongs(name, 1).firstOrNull()?.artwork?.takeIf { it.isNotBlank() } }
            .getOrNull()
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

    /**
     * Runs several album/movie searches in parallel, merges + de-dupes.
     * Uses the album-aware [search] so movie names surface their full soundtrack
     * with the ORIGINAL poster art (instead of shared compilation covers).
     * Interleaves songs from each source so the feed isn't all one movie at the top.
     */
    private suspend fun searchMix(
        queries: List<String>,
        perQuery: Int = 10,
        minDuration: Int = 90,
        limit: Int = 60
    ): List<Song> = coroutineScope {
        val perSource = queries.map { q ->
            async { runCatching { search(q, perQuery).filter { it.duration >= minDuration } }.getOrDefault(emptyList()) }
        }.map { it.await() }

        // Round-robin interleave for variety.
        val seen = HashSet<String>()
        val merged = ArrayList<Song>()
        var idx = 0
        var added = true
        while (added && merged.size < limit) {
            added = false
            for (source in perSource) {
                if (idx < source.size) {
                    val song = source[idx]
                    if (seen.add(song.id)) merged.add(song)
                    added = true
                }
            }
            idx++
        }
        merged.take(limit)
    }

    // Movie / album name queries -> each returns songs with their ORIGINAL poster art.
    // (Generic "trending/top/hits" queries return compilation covers, all the same image.)
    private val HOME_QUERIES = listOf(
        "Animal", "Stree 2", "Bhediya", "Munjya", "Teri Baaton Mein Aisa Uljha Jiya",
        "Jawan", "Rockstar", "Aashiqui 2", "Kabir Singh", "Sanam Teri Kasam",
        "Chandu Champion", "Bad Newz"
    )
    private val TRENDING_QUERIES = listOf(
        "Stree 2", "Animal", "Pushpa 2", "Munjya", "Bhediya",
        "Teri Baaton Mein Aisa Uljha Jiya", "Kalki 2898 AD", "Bad Newz", "Jawan"
    )
    private val NEW_RELEASE_QUERIES = listOf(
        "Bhool Bhulaiyaa 3", "Singham Again", "Pushpa 2", "Vicky Vidya Ka Woh Wala Video",
        "Devara", "Amar Singh Chamkila", "Sky Force", "Chhaava", "Sanam Teri Kasam"
    )
    private val TOP_QUERIES = listOf(
        "Aashiqui 2", "Kabir Singh", "Rockstar", "Yeh Jawaani Hai Deewani",
        "Ae Dil Hai Mushkil", "Sanam Teri Kasam", "Half Girlfriend", "Arjun Reddy"
    )

    suspend fun home(languages: Set<String> = setOf("hindi")): List<Song> =
        searchMix(HOME_QUERIES, perQuery = 10, minDuration = 120, limit = 60)

    suspend fun trending(languages: Set<String> = setOf("hindi")): List<Song> =
        searchMix(TRENDING_QUERIES, perQuery = 12, minDuration = 90, limit = 40)

    suspend fun newReleases(languages: Set<String> = setOf("hindi")): List<Song> =
        searchMix(NEW_RELEASE_QUERIES, perQuery = 12, minDuration = 90, limit = 40)

    suspend fun topCharts(languages: Set<String> = setOf("hindi")): List<Song> =
        searchMix(TOP_QUERIES, perQuery = 12, minDuration = 90, limit = 40)

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
