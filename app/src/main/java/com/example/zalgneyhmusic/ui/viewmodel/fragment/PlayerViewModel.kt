package com.example.zalgneyhmusic.ui.viewmodel.fragment

import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import com.example.zalgneyhmusic.data.session.UserManager
import com.example.zalgneyhmusic.player.MusicPlayer
import com.example.zalgneyhmusic.player.RepeatMode
import com.example.zalgneyhmusic.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for Music Player
 * Manages player UI state and logic
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicPlayer: MusicPlayer,
    private val userManager: UserManager,
    private val musicRepository: MusicRepository
) : BaseViewModel() {

    companion object {
        private const val POSITION_UPDATE_INTERVAL_MS = 1000L
        private const val MILLIS_TO_SECONDS = 1000
        private const val SECONDS_TO_MINUTES = 60
    }

    // Expose player states
    val currentSong: StateFlow<Song?> = musicPlayer.currentSong
    val isPlaying: StateFlow<Boolean> = musicPlayer.isPlaying
    val currentPosition: StateFlow<Int> = musicPlayer.currentPosition
    val duration: StateFlow<Int> = musicPlayer.duration
    val shuffleMode: StateFlow<Boolean> = musicPlayer.shuffleMode
    val repeatMode: StateFlow<RepeatMode> = musicPlayer.repeatMode
    val playlist: StateFlow<List<Song>> = musicPlayer.playlist

    private var positionUpdateJob: Job? = null

    val isCurrentSongFavorite: StateFlow<Boolean> = combine(
        musicPlayer.currentSong,
        userManager.favoriteSongIds
    ) { song, favIds ->
        if (song == null) false else favIds.contains(song.id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        startPositionUpdates()
        refreshFavorites()
    }

    /**
     * Loads favorite songs from API if not already cached.
     */
    private fun refreshFavorites() {
        viewModelScope.launch {
            // Load data from API if not already available
            if (userManager.favoriteSongIds.value.isEmpty()) {
                musicRepository.getMyPlaylists()
            }
        }
    }

    /**
     * Set playlist and start playback
     */
    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        musicPlayer.setPlaylist(songs, startIndex)
    }

    /**
     * Play/Pause toggle
     */
    fun togglePlayPause() {
        musicPlayer.togglePlayPause()
    }

    /**
     * Play
     */
    fun play() {
        musicPlayer.play()
    }

    /**
     * Next song
     */
    fun next() {
        musicPlayer.next()
    }

    /**
     * Previous song
     */
    fun previous() {
        musicPlayer.previous()
    }

    /**
     * Seek to position
     */
    fun seekTo(position: Int) {
        musicPlayer.seekTo(position)
    }

    /**
     * Toggle shuffle
     */
    fun toggleShuffle() {
        musicPlayer.toggleShuffle()
    }

    /**
     * Toggle repeat mode
     */
    fun toggleRepeat() {
        musicPlayer.toggleRepeat()
    }

    /**
     * add song to next
     * */
    fun addSongToNext(song: Song) {
        musicPlayer.addSongToNext(song)
    }


    /**
     * add song to player list
     * */
    fun addSongToQueue(song: Song) {
        musicPlayer.addSongToQueue(song)
    }

    /**
     * Start updating position every second
     */
    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (isActive) {
                musicPlayer.updatePosition()
                delay(POSITION_UPDATE_INTERVAL_MS)
            }
        }
    }

    /**
     * Formats milliseconds to MM:SS format
     */
    fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / MILLIS_TO_SECONDS
        val minutes = totalSeconds / SECONDS_TO_MINUTES
        val seconds = totalSeconds % SECONDS_TO_MINUTES
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        positionUpdateJob?.cancel()
        // Note: Don't release musicPlayer here as it's a Singleton
        // and may be used elsewhere
    }
}
