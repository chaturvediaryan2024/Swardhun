package com.aryan.calculator.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.aryan.calculator.data.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RepeatMode { OFF, ONE, ALL }

data class PlaybackState(
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val playbackSpeed: Float = 1f
) {
    val currentSong: Song? get() = queue.getOrNull(currentIndex)
}

/** Shared MediaController wrapper connected to [PlaybackService], exposed as a
 *  StateFlow so any Compose screen can observe/drive playback without its own session. */
class PlayerController(context: Context) {

    private val appContext = context.applicationContext
    private var controller: MediaController? = null

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) = updateState()
        override fun onPlaybackStateChanged(playbackState: Int) = updateState()
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _state.value = _state.value.copy(currentIndex = controller?.currentMediaItemIndex ?: -1)
            updateState()
        }
    }

    init {
        val token = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
        val future = MediaController.Builder(appContext, token).buildAsync()
        future.addListener({
            controller = future.get().also { it.addListener(playerListener) }
        }, ContextCompat.getMainExecutor(appContext))
    }

    fun playQueue(songs: List<Song>, startIndex: Int, shuffle: Boolean = false) {
        val c = controller ?: return
        val actualSongs: List<Song>
        val actualIndex: Int

        if (shuffle && songs.size > 1) {
            val selectedSong = songs[startIndex]
            val shuffledOthers = songs.filterIndexed { i, _ -> i != startIndex }.shuffled()
            actualSongs = listOf(selectedSong) + shuffledOthers
            actualIndex = 0
            c.shuffleModeEnabled = true
            _state.value = _state.value.copy(shuffleEnabled = true)
        } else {
            actualSongs = songs
            actualIndex = startIndex
        }

        val items = actualSongs.map { it.toMediaItem() }
        c.setMediaItems(items, actualIndex, 0L)
        c.prepare()
        c.play()
        _state.value = _state.value.copy(queue = actualSongs, currentIndex = actualIndex)
    }

    fun addToQueue(song: Song) {
        val c = controller ?: return
        c.addMediaItem(song.toMediaItem())
        _state.value = _state.value.copy(queue = _state.value.queue + song)
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() {
        val c = controller ?: return
        val queue = _state.value.queue
        if (queue.isEmpty()) return

        val currentIdx = _state.value.currentIndex
        val nextIdx = if (currentIdx < queue.size - 1) {
            currentIdx + 1
        } else if (_state.value.repeatMode == RepeatMode.ALL) {
            0
        } else {
            return
        }

        c.seekTo(nextIdx, 0L)
        _state.value = _state.value.copy(currentIndex = nextIdx)
    }

    fun previous() {
        val c = controller ?: return
        val queue = _state.value.queue
        if (queue.isEmpty()) return

        if (c.currentPosition > 3000) {
            c.seekTo(0L)
            return
        }

        val currentIdx = _state.value.currentIndex
        val prevIdx = if (currentIdx > 0) {
            currentIdx - 1
        } else if (_state.value.repeatMode == RepeatMode.ALL) {
            queue.size - 1
        } else {
            0
        }

        c.seekTo(prevIdx, 0L)
        _state.value = _state.value.copy(currentIndex = prevIdx)
    }

    fun seekTo(positionMs: Long) = controller?.seekTo(positionMs) ?: Unit

    fun setPlaybackSpeed(speed: Float) {
        val c = controller ?: return
        c.setPlaybackSpeed(speed)
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    fun pause() {
        controller?.pause()
    }

    fun toggleShuffle() {
        val c = controller ?: return
        val newShuffle = !c.shuffleModeEnabled
        c.shuffleModeEnabled = newShuffle
        _state.value = _state.value.copy(shuffleEnabled = newShuffle)

        if (newShuffle && _state.value.queue.size > 1) {
            val currentSong = _state.value.currentSong
            if (currentSong != null) {
                val others = _state.value.queue.filter { it.id != currentSong.id }.shuffled()
                val newQueue = listOf(currentSong) + others
                val items = newQueue.map { it.toMediaItem() }
                c.setMediaItems(items, 0, c.currentPosition)
                _state.value = _state.value.copy(queue = newQueue, currentIndex = 0)
            }
        }
    }

    fun cycleRepeatMode() {
        val c = controller ?: return
        val newMode = when (_state.value.repeatMode) {
            RepeatMode.OFF -> {
                c.repeatMode = Player.REPEAT_MODE_ALL
                RepeatMode.ALL
            }
            RepeatMode.ALL -> {
                c.repeatMode = Player.REPEAT_MODE_ONE
                RepeatMode.ONE
            }
            RepeatMode.ONE -> {
                c.repeatMode = Player.REPEAT_MODE_OFF
                RepeatMode.OFF
            }
        }
        _state.value = _state.value.copy(repeatMode = newMode)
    }

    /** Call periodically (e.g. once a second) from a Composable's LaunchedEffect to drive the seek bar. */
    fun tickPosition() {
        val c = controller ?: return
        _state.value = _state.value.copy(
            positionMs = c.currentPosition.coerceAtLeast(0L),
            durationMs = c.duration.coerceAtLeast(0L)
        )
    }

    fun release() {
        controller?.release()
        controller = null
    }

    private fun updateState() {
        val c = controller ?: return
        _state.value = _state.value.copy(
            isPlaying = c.isPlaying,
            isBuffering = c.playbackState == Player.STATE_BUFFERING,
            positionMs = c.currentPosition.coerceAtLeast(0L),
            durationMs = c.duration.coerceAtLeast(0L)
        )
    }

    private fun Song.toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setUri(streamUrl)
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setArtworkUri(artwork.takeIf { it.isNotBlank() }?.let { Uri.parse(it) })
                    .build()
            )
            .build()
}
