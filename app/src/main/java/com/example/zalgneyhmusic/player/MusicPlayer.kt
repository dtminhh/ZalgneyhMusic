package com.example.zalgneyhmusic.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.service.MusicService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Music Player Manager - Updated to work with MusicService (ExoPlayer)
 * Acts as a client to the MusicService
 */
@Singleton
class MusicPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer // Inject shared ExoPlayer instance
) {
    private var musicService: MusicService? = null
    private var isBound = false

    // StateFlows for UI observation
    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist.asStateFlow()

    private val _currentIndex = MutableStateFlow<Int>(-1)

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

    // Job for updating seekbar progress
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    // Service connection
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            syncState() // Sync state when reconnected
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }

    init {
        bindService()
        setupExoPlayerListeners()
    }

    private fun bindService() {
        val intent = Intent(context, MusicService::class.java)
        // Start service so it runs in background even when UI unbinds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        // Bind to call functions
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    /**
     * Listen to events from ExoPlayer to update UI.
     * (Since ExoPlayer is Singleton, we can listen directly instead of via Service callback)
     */
    private fun setupExoPlayerListeners() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = exoPlayer.duration.toInt()
                    updateCurrentSongInfo()
                }
                updatePlayingState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                updatePlayingState()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentSongInfo()
            }
        })
    }

    // --- Synchronization Logic ---

    private fun updateCurrentSongInfo() {
        val currentIdx = exoPlayer.currentMediaItemIndex
        val currentList = _playlist.value

        if (currentIdx in currentList.indices) {
            _currentIndex.value = currentIdx
            _currentSong.value = currentList[currentIdx]
        }
    }

    private fun updatePlayingState() {
        if (exoPlayer.isPlaying) {
            startProgressUpdate()
        } else {
            stopProgressUpdate()
        }
    }

    private fun syncState() {
        _isPlaying.value = exoPlayer.isPlaying
        _duration.value = exoPlayer.duration.coerceAtLeast(0).toInt()
        _currentPosition.value = exoPlayer.currentPosition.toInt()
        updateCurrentSongInfo()
    }

    private fun startProgressUpdate() {
        if (progressJob?.isActive == true) return
        progressJob = scope.launch {
            while (isActive) {
                _currentPosition.value = exoPlayer.currentPosition.toInt()
                delay(500) // Update every 0.5s
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        _currentPosition.value = exoPlayer.currentPosition.toInt()
    }

    // --- Public Methods (Call to MusicService) ---

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return

        // Update local state immediately
        _playlist.value = songs

        // Call Service to play music & show notification
        musicService?.setPlaylist(songs, startIndex)
    }

    fun play() {
        musicService?.play()
    }

    fun pause() {
        musicService?.pause()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) pause() else play()
    }

    fun next() {
        musicService?.playNext()
    }

    fun previous() {
        musicService?.playPrevious()
    }

    fun seekTo(position: Int) {
        musicService?.seekTo(position.toLong())
        _currentPosition.value = position
    }

    // --- Queue Management (Directly handle on ExoPlayer & Local List) ---

    fun addSongToNext(song: Song) {
        // Update internal list
        val currentList = _playlist.value.toMutableList()
        val currentIdx = _currentIndex.value
        val insertIndex = currentIdx + 1

        if (insertIndex <= currentList.size) {
            currentList.add(insertIndex, song)
        } else {
            currentList.add(song)
        }
        _playlist.value = currentList

        // Update ExoPlayer (Add to actual queue)
        val mediaItem = MediaItem.Builder()
            .setUri(song.url)
            .setMediaId(song.id)
            .setTag(song)
            .build()

        if (insertIndex <= exoPlayer.mediaItemCount) {
            exoPlayer.addMediaItem(insertIndex, mediaItem)
        } else {
            exoPlayer.addMediaItem(mediaItem)
        }
    }

    fun addSongToQueue(song: Song) {
        val currentList = _playlist.value.toMutableList()
        currentList.add(song)
        _playlist.value = currentList

        val mediaItem = MediaItem.Builder()
            .setUri(song.url)
            .setMediaId(song.id)
            .setTag(song)
            .build()

        exoPlayer.addMediaItem(mediaItem)

        // If not playing anything, play this song immediately
        if (exoPlayer.playbackState == Player.STATE_IDLE || exoPlayer.mediaItemCount == 1) {
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    fun toggleShuffle() {
        _shuffleMode.value = !_shuffleMode.value
        exoPlayer.shuffleModeEnabled = _shuffleMode.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.NONE -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.NONE
        }

        // Map to ExoPlayer repeat mode
        exoPlayer.repeatMode = when (_repeatMode.value) {
            RepeatMode.NONE -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }
}

/**
 * Repeat modes
 */
enum class RepeatMode {
    NONE,   // No repeat
    ONE,    // Repeat current song
    ALL     // Repeat entire playlist
}