package com.example.zalgneyhmusic.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import com.example.zalgneyhmusic.data.model.domain.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Music Player Manager - Manage music playback with MediaPlayer
 * Supports: play, pause, seek, next, previous, shuffle, repeat
 */
@Singleton
class MusicPlayer @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val RESTART_SONG_THRESHOLD_MS = 3000
        private const val DUCK_VOLUME = 0.3f
        private const val NORMAL_VOLUME = 1.0f
    }

    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    // Track if MediaPlayer is prepared
    private var isPrepared = false

    // Current playlist and position
    private var currentPlaylist: List<Song> = emptyList()
    private var currentIndex: Int = -1

    // Player states
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.NONE)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initializeAudioFocus()
    }

    private fun initializeAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener { focusChange ->
                    handleAudioFocusChange(focusChange)
                }
                .build()
        }
    }

    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pause()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pause()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(DUCK_VOLUME, DUCK_VOLUME)
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(NORMAL_VOLUME, NORMAL_VOLUME)
            }
        }
    }

    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { request ->
                audioManager?.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } ?: false
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                { handleAudioFocusChange(it) },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { request ->
                audioManager?.abandonAudioFocusRequest(request)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus { handleAudioFocusChange(it) }
        }
    }

    /**
     * Set playlist and play first song
     */
    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) {
            Log.w("MusicPlayer", "Cannot set empty playlist")
            return
        }
        currentPlaylist = songs
        currentIndex = startIndex.coerceIn(0, songs.size - 1)
        playSongAtIndex(currentIndex)
    }

    /**
     * Play song at index
     */
    private fun playSongAtIndex(index: Int) {
        if (index < 0 || index >= currentPlaylist.size) {
            Log.w("MusicPlayer", "Invalid index: $index")
            return
        }

        currentIndex = index
        val song = currentPlaylist[index]
        _currentSong.value = song

        try {
            releaseMediaPlayer()
            isPrepared = false

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                try {
                    setDataSource(song.url)
                    prepareAsync()
                } catch (e: Exception) {
                    Log.e("MusicPlayer", "Error setting data source: ${song.url}", e)
                    return
                }

                setOnPreparedListener { mp ->
                    isPrepared = true
                    _duration.value = mp.duration
                    Log.d("MusicPlayer", "Song prepared: ${song.title}, duration: ${mp.duration}")
                    play()
                }

                setOnCompletionListener {
                    onSongComplete()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(
                        "MusicPlayer",
                        "MediaPlayer error: what=$what, extra=$extra, song=${song.url}"
                    )
                    isPrepared = false
                    next()
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Error playing song: ${song.title}", e)
            isPrepared = false
        }
    }

    /**
     * Play/Resume
     */
    fun play() {
        try {
            mediaPlayer?.let { mp ->
                if (!isPrepared) {
                    Log.w("MusicPlayer", "MediaPlayer not prepared yet")
                    return
                }

                if (mp.isPlaying) {
                    Log.d("MusicPlayer", "Already playing")
                    return
                }

                if (requestAudioFocus()) {
                    mp.start()
                    _isPlaying.value = true
                    Log.d("MusicPlayer", "Playback started")
                } else {
                    Log.w("MusicPlayer", "Failed to get audio focus")
                }
            } ?: run {
                Log.w("MusicPlayer", "MediaPlayer is null, cannot play")
                // If we have a playlist but no MediaPlayer, try to start playing
                if (currentPlaylist.isNotEmpty() && currentIndex >= 0) {
                    playSongAtIndex(currentIndex)
                }
            }
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Error in play()", e)
            _isPlaying.value = false
        }
    }

    /**
     * Pause
     */
    fun pause() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.pause()
                    _isPlaying.value = false
                    Log.d("MusicPlayer", "Playback paused")
                }
            }
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Error in pause()", e)
            _isPlaying.value = false
        }
    }

    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    /**
     * Next song
     */
    fun next() {
        val nextIndex = when {
            _shuffleMode.value -> {
                (0 until currentPlaylist.size).random()
            }

            currentIndex + 1 < currentPlaylist.size -> {
                currentIndex + 1
            }

            else -> 0 // Loop to first song
        }
        playSongAtIndex(nextIndex)
    }

    /**
     * Previous song
     */
    fun previous() {
        // If playing more than threshold, restart current song
        if ((mediaPlayer?.currentPosition ?: 0) > RESTART_SONG_THRESHOLD_MS) {
            seekTo(0)
        } else {
            val prevIndex = if (currentIndex - 1 >= 0) {
                currentIndex - 1
            } else {
                currentPlaylist.size - 1
            }
            playSongAtIndex(prevIndex)
        }
    }

    /**
     * Seek to position (milliseconds)
     */
    fun seekTo(position: Int) {
        try {
            mediaPlayer?.let { mp ->
                if (isPrepared) {
                    mp.seekTo(position)
                    _currentPosition.value = position
                    Log.d("MusicPlayer", "Seeked to: $position")
                }
            }
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Error in seekTo()", e)
        }
    }

    /**
     * Toggle shuffle mode
     */
    fun toggleShuffle() {
        _shuffleMode.value = !_shuffleMode.value
    }

    /**
     * Toggle repeat mode: NONE -> ONE -> ALL -> NONE
     */
    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.NONE -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.NONE
        }
    }

    /**
     * Handles playback completion
     */
    private fun onSongComplete() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0)
                play()
            }

            RepeatMode.ALL -> {
                next()
            }

            RepeatMode.NONE -> {
                if (currentIndex + 1 < currentPlaylist.size) {
                    next()
                } else {
                    pause()
                    _currentPosition.value = 0
                }
            }
        }
    }

    /**
     * Update current position
     */
    fun updatePosition() {
        try {
            mediaPlayer?.let { mp ->
                if (isPrepared && mp.isPlaying) {
                    _currentPosition.value = mp.currentPosition
                }
            }
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Error updating position", e)
        }
    }

    /**
     * Release resources
     */
    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Error releasing MediaPlayer", e)
        } finally {
            mediaPlayer = null
            isPrepared = false
        }
    }

    /**
     * Clean up
     */
    fun release() {
        releaseMediaPlayer()
        abandonAudioFocus()
        _isPlaying.value = false
    }
}

/**
 * Repeat modes
 */
enum class RepeatMode {
    NONE,   // non-repeat
    ONE,    // Repeat current song
    ALL     // Repeat in playlist
}