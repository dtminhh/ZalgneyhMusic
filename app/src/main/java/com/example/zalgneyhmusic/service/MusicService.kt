package com.example.zalgneyhmusic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.zalgneyhmusic.MainActivity
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.domain.Song
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Foreground service for background music playback
 * Manages ExoPlayer, playlist, and media notification
 */
@AndroidEntryPoint
class MusicService : Service() {

    // ExoPlayer instance injected by Hilt
    @Inject
    lateinit var exoPlayer: ExoPlayer

    // Binder for Activity/Fragment to control this service
    private val binder = MusicBinder()

    private var currentPlaylist: List<Song> = emptyList()
    private var currentSongIndex: Int = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupExoPlayerListeners()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    /** Creates notification channel for Android O+ */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",  // Display name
                NotificationManager.IMPORTANCE_LOW  // No vibration, no sound
            ).apply {
                description = "Shows currently playing music"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /** Setup ExoPlayer event listeners */
    private fun setupExoPlayerListeners() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> playNext()
                    Player.STATE_READY -> updateNotification()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification()
            }
        })
    }

    // ==================== Music Control Methods ====================

    /**
     * Sets playlist and starts playback
     * @param songs List of songs to play
     * @param startIndex Index to start from
     */
    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        currentPlaylist = songs
        currentSongIndex = startIndex

        exoPlayer.clearMediaItems()

        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setUri(song.url)
                .setMediaId(song.id)
                .build()
        }
        exoPlayer.addMediaItems(mediaItems)
        exoPlayer.seekTo(startIndex, 0)
        exoPlayer.prepare()
        exoPlayer.play()

        startForeground(NOTIFICATION_ID, createNotification())
    }

    fun play() = exoPlayer.play()

    fun pause() = exoPlayer.pause()

    fun playNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNext()
            currentSongIndex = (currentSongIndex + 1) % currentPlaylist.size
        }
    }

    fun playPrevious() {
        if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPrevious()
            currentSongIndex = if (currentSongIndex > 0) currentSongIndex - 1
            else currentPlaylist.size - 1
        }
    }

    fun seekTo(positionMs: Long) = exoPlayer.seekTo(positionMs)

    fun getCurrentPosition(): Long = exoPlayer.currentPosition

    fun getDuration(): Long = exoPlayer.duration

    fun getCurrentSong(): Song? = currentPlaylist.getOrNull(currentSongIndex)

    fun isPlaying(): Boolean = exoPlayer.isPlaying

    // ==================== Notification ====================

    /** Creates media notification */
    private fun createNotification(): Notification {
        val currentSong = getCurrentSong()
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentSong?.title ?: "No song")
            .setContentText(currentSong?.artist?.name ?: "Unknown artist")
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            // TODO: Add action buttons (Previous, Play/Pause, Next)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

    companion object {
        private const val CHANNEL_ID = "music_playback_channel"
        private const val NOTIFICATION_ID = 1
    }
}