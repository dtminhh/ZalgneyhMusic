package com.example.zalgneyhmusic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.zalgneyhmusic.MainActivity
import com.example.zalgneyhmusic.R
import androidx.core.app.ServiceCompat
import com.example.zalgneyhmusic.data.model.domain.Song
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : Service() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    private val binder = MusicBinder()
    private lateinit var mediaSession: MediaSessionCompat

    private var currentPlaylist: List<Song> = emptyList()
    private var currentSongIndex: Int = 0
    private var currentLargeIcon: Bitmap? = null

    companion object {
        const val CHANNEL_ID = "music_channel_id"
        const val NOTIFICATION_ID = 1

        const val ACTION_PREVIOUS = "com.example.zalgneyhmusic.ACTION_PREVIOUS"
        const val ACTION_PLAY_PAUSE = "com.example.zalgneyhmusic.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.zalgneyhmusic.ACTION_NEXT"
        const val ACTION_STOP = "com.example.zalgneyhmusic.ACTION_STOP"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize MediaSession
        mediaSession = MediaSessionCompat(this, "MusicServiceTag").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() { play() }
                override fun onPause() { pause() }
                override fun onSkipToNext() { playNext() }
                override fun onSkipToPrevious() { playPrevious() }
                override fun onSeekTo(pos: Long) { seekTo(pos) }
            })
            isActive = true
        }

        createNotificationChannel()

        // Configure audio focus for media playback
        val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        exoPlayer.setAudioAttributes(audioAttributes, true)

        setupExoPlayerListeners()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // [ANR FIX] Always start foreground immediately to avoid being killed in the first 5 seconds
        val notification = getNotificationBuilder(getCurrentSong(), currentLargeIcon).build()
        startForeground(NOTIFICATION_ID, notification)

        when (intent?.action) {
            ACTION_PREVIOUS -> playPrevious()
            ACTION_PLAY_PAUSE -> if (exoPlayer.isPlaying) pause() else play()
            ACTION_NEXT -> playNext()
            ACTION_STOP -> stopSelf()
        }
        return START_NOT_STICKY
    }

    // Streamlined builder; removed redundant Intent declarations
    private fun getNotificationBuilder(song: Song?, bitmap: Bitmap?): NotificationCompat.Builder {
        val isPlaying = exoPlayer.isPlaying
        val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevIntent = PendingIntent.getService(this, 0, Intent(this, MusicService::class.java).setAction(ACTION_PREVIOUS), PendingIntent.FLAG_IMMUTABLE)
        val playPauseIntent = PendingIntent.getService(this, 1, Intent(this, MusicService::class.java).setAction(ACTION_PLAY_PAUSE), PendingIntent.FLAG_IMMUTABLE)
        val nextIntent = PendingIntent.getService(this, 2, Intent(this, MusicService::class.java).setAction(ACTION_NEXT), PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(song?.title ?: getString(R.string.app_name))
            .setContentText(song?.artist?.name ?: "Ready to play")
            .setLargeIcon(bitmap ?: BitmapFactory.decodeResource(resources, R.drawable.ic_album_placeholder))
            .setContentIntent(contentPendingIntent)
            .setOngoing(isPlaying) // Make non-dismissible only while playing
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .addAction(R.drawable.ic_skip_previous, "Prev", prevIntent)
            .addAction(playPauseIcon, "Play/Pause", playPauseIntent)
            .addAction(R.drawable.ic_skip_next, "Next", nextIntent)
    }

    private fun updateNotification() {
        val song = getCurrentSong()

        // 1) Update notification UI immediately
        val notification = getNotificationBuilder(song, currentLargeIcon).build()
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (exoPlayer.isPlaying) {
            // Keep service in foreground while playing
            startForeground(NOTIFICATION_ID, notification)
        } else {
            // When paused, detach foreground but keep showing the notification via notify()
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
            notificationManager.notify(NOTIFICATION_ID, notification)
        }

        // 2) Lazy-load cover art if missing
        if (song != null && currentLargeIcon == null && song.imageUrl.isNotEmpty()) {
            Glide.with(this)
                .asBitmap()
                .load(song.imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        // Ensure the track hasn't changed during bitmap loading
                        if (getCurrentSong()?.id == song.id) {
                            currentLargeIcon = resource
                            updateMediaSessionMetadata()

                            // Rebuild and update notification with the loaded artwork
                            val updatedNotif = getNotificationBuilder(song, resource).build()
                            notificationManager.notify(NOTIFICATION_ID, updatedNotif)
                        }
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {}
                    override fun onLoadFailed(errorDrawable: Drawable?) {}
                })
        }
    }

    private fun setupExoPlayerListeners() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    playNext()
                }
                if (playbackState == Player.STATE_READY) {
                    updateMediaSessionMetadata()
                }
                updateMediaSessionState()
                updateNotification()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateMediaSessionState()
                updateNotification()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (mediaItem != null) {
                    currentSongIndex = exoPlayer.currentMediaItemIndex
                    currentLargeIcon = null // Reset cached artwork when switching tracks

                    updateMediaSessionMetadata()
                    updateMediaSessionState()
                    updateNotification()
                }
            }

            override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                updateMediaSessionState()
            }
        })
    }

    private fun updateMediaSessionState() {
        val playbackState = if (exoPlayer.isPlaying)
            PlaybackStateCompat.STATE_PLAYING
        else
            PlaybackStateCompat.STATE_PAUSED

        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    playbackState,
                    exoPlayer.currentPosition,
                    1f,
                    SystemClock.elapsedRealtime()
                )
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_SEEK_TO
                )
                .build()
        )
    }

    private fun updateMediaSessionMetadata() {
        val song = getCurrentSong() ?: return
        val duration = if (exoPlayer.duration != C.TIME_UNSET) exoPlayer.duration else -1L

        val builder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist.name)

        if (duration > 0) {
            builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
        }

        if (currentLargeIcon != null) {
            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, currentLargeIcon)
        }

        mediaSession.setMetadata(builder.build())
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        currentPlaylist = songs
        currentSongIndex = startIndex
        currentLargeIcon = null

        exoPlayer.clearMediaItems()
        val mediaItems = songs.map { song ->
            val uri = if (!song.localPath.isNullOrEmpty()) {
                val file = java.io.File(song.localPath)
                if (file.exists()) android.net.Uri.fromFile(file) else song.url.toUri()
            } else {
                song.url.toUri()
            }

            MediaItem.Builder()
                .setUri(uri)
                .setMediaId(song.id)
                .setTag(song)
                .build()
        }
        exoPlayer.setMediaItems(mediaItems, startIndex, 0)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun play() {
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun playNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNext()
        } else {
            if (currentPlaylist.isNotEmpty()) {
                exoPlayer.seekTo(0, 0)
                pause()
            }
        }
    }

    fun playPrevious() {
        if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPrevious()
        } else {
            if (currentPlaylist.isNotEmpty()) {
                exoPlayer.seekTo(0, 0)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    fun getCurrentSong(): Song? {
        val item = exoPlayer.currentMediaItem ?: return null
        return item.localConfiguration?.tag as? Song
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Media Playback Controls"
            channel.setShowBadge(false)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
        exoPlayer.release()
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}