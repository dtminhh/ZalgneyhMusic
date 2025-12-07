package com.example.zalgneyhmusic.ui.handler

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.DetailType
import com.example.zalgneyhmusic.data.model.domain.Playlist
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import com.example.zalgneyhmusic.data.session.UserManager
import com.example.zalgneyhmusic.ui.moreoptions.MoreOptionsAction
import com.example.zalgneyhmusic.ui.moreoptions.MoreOptionsManager
import com.example.zalgneyhmusic.ui.navigation.DetailNavigator
import com.example.zalgneyhmusic.ui.viewmodel.fragment.PlayerViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Class that handles ALL menu actions (3 dots) and Play music.
 * Helps Fragment not to repeat handleAction code.
 */
class MediaActionHandler(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val playerViewModel: PlayerViewModel,
    private val navigator: DetailNavigator?,
    private val musicRepository: MusicRepository,
    private val scope: CoroutineScope,
    private val userManager: UserManager,
) {

    // ==================== SONG ====================
    fun onSongMenuClick(song: Song) {
        // Get status from UserManager (cached data is always up-to-date)
        val isFav = userManager.isSongFavorite(song.id)

        // Debug log
        android.util.Log.d("DEBUG_FAV", "Menu for ${song.title}, isFav: $isFav")

        MoreOptionsManager.showForSong(fragmentManager, song, isFav) { action ->
            handleSongAction(action, song)
        }
    }

    fun onSongClick(song: Song, playlist: List<Song> = listOf(song)) {
        val index = playlist.indexOfFirst { it.id == song.id }.takeIf { it != -1 } ?: 0
        playerViewModel.setPlaylist(playlist, index)
    }

    private fun handleSongAction(action: MoreOptionsAction.SongAction, song: Song) {
        when (action) {
            is MoreOptionsAction.SongAction.PlayNext -> {
                playerViewModel.addSongToNext(song)
                Toast.makeText(context, context.getString(R.string.toast_added_to_play_next), Toast.LENGTH_SHORT).show()
            }

            is MoreOptionsAction.SongAction.AddToQueue -> {
                playerViewModel.addSongToQueue(song)
                Toast.makeText(context, context.getString(R.string.toast_added_to_queue), Toast.LENGTH_SHORT).show()
            }

            is MoreOptionsAction.SongAction.AddToPlaylist -> {
                showAddToPlaylistDialog(song)
            }

            is MoreOptionsAction.SongAction.AddToFavorite -> {
                toggleFavorite(song)
            }

            is MoreOptionsAction.SongAction.RemoveFromFavorites -> toggleFavorite(song)

            is MoreOptionsAction.SongAction.GoToArtist -> {
                navigator?.navigatorToDetailScreen(DetailType.Artist(song.artist.id))
            }

            is MoreOptionsAction.SongAction.GoToAlbum -> {
                song.album?.id?.let { albumId ->
                    navigator?.navigatorToDetailScreen(DetailType.Album(albumId))
                } ?: Toast.makeText(context, context.getString(R.string.toast_unknown_album), Toast.LENGTH_SHORT).show()
            }

            is MoreOptionsAction.SongAction.Share -> {
                shareContent(
                    song.title,
                    context.getString(R.string.share_song_message, song.title, song.artist.name)
                )
            }



            else -> {}
        }
    }

    /**
     * Shows dialog to select a playlist to add the song to.
     */
    private fun showAddToPlaylistDialog(song: Song) {
        scope.launch {
            // Load user's playlists
            val result = musicRepository.getMyPlaylists()
            if (result is Resource.Success) {
                val playlists = result.result
                withContext(Dispatchers.Main) {
                    showSelectPlaylistDialog(playlists) { selectedPlaylist ->
                        addSongToPlaylist(selectedPlaylist.id, song.id)
                    }
                }
            }
        }
    }

    /**
     * Displays dialog for selecting a playlist.
     *
     * @param playlists List of available playlists
     * @param onPlaylistSelected Callback when a playlist is selected
     */
    private fun showSelectPlaylistDialog(
        playlists: List<Playlist>,
        onPlaylistSelected: (Playlist) -> Unit
    ) {
        if (playlists.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.toast_no_playlists), Toast.LENGTH_SHORT).show()
            return
        }

        // Create array of playlist names for display
        val playlistNames = playlists.map { it.name }.toTypedArray()

        // Create dialog
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.dialog_add_to_playlist_title))
            .setItems(playlistNames) { dialog, which ->
                // 'which' is the index of selected item
                val selectedPlaylist = playlists[which]

                // Call callback to handle adding song
                onPlaylistSelected(selectedPlaylist)

                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .show()
    }

    private fun addSongToPlaylist(playlistId: String, songId: String) {
        scope.launch {
            val result = musicRepository.addSongToPlaylist(playlistId, songId)

            withContext(Dispatchers.Main) {
                if (result is Resource.Success) {
                    Toast.makeText(context, context.getString(R.string.toast_added_to_playlist), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.toast_failed_add_to_playlist), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Toggles artist follow status with optimistic UI update.
     *
     * @param artist Artist to follow/unfollow
     */
    fun toggleFollowArtist(artist: Artist) {
        scope.launch {
            // Get current follow status
            val isCurrentlyFollowed = userManager.isArtistFollowed(artist.id)

            // [OPTIMISTIC] Update UI immediately
            if (isCurrentlyFollowed) {
                userManager.unfollowArtist(artist.id)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_unfollowed_artist, artist.name),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                userManager.followArtist(artist.id)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_following_artist, artist.name),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // Call API in background
            val result = musicRepository.toggleFollowArtist(artist.id)

            // Handle errors (revert if failed)
            if (result is Resource.Failure) {
                withContext(Dispatchers.Main) {
                    if (isCurrentlyFollowed) userManager.followArtist(artist.id)
                    else userManager.unfollowArtist(artist.id)
                    Toast.makeText(context, context.getString(R.string.toast_connection_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Toggles song favorite status with optimistic UI update.
     *
     * @param song Song to add/remove from favorites
     */
    fun toggleFavorite(song: Song) {
        val favId = userManager.favoritePlaylistId

        if (favId.isNullOrEmpty()) {
            Toast.makeText(
                context,
                context.getString(R.string.toast_syncing_data),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        scope.launch {
            // Get current favorite status
            val isCurrentlyFav = userManager.isSongFavorite(song.id)

            // [OPTIMISTIC UPDATE] Update UI immediately (don't wait for server)
            if (isCurrentlyFav) {
                userManager.removeFavoriteSong(song.id)
            } else {
                userManager.addFavoriteSong(song.id)
            }
            // PlayerFragment and MoreOptions will automatically update icon/text

            // Call API in background
            val result = musicRepository.toggleFavorite(favId, song.id)

            withContext(Dispatchers.Main) {
                when (result) {
                    is Resource.Success -> {
                        // API successful: Verify result from server
                        val serverAdded = result.result

                        // Show notification
                        val msg = if (serverAdded) {
                            context.getString(R.string.toast_added_to_favorites)
                        } else {
                            context.getString(R.string.toast_removed_from_favorites)
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                        // (Optional) Sync again if server result differs from prediction (rare case)
                        if (serverAdded != !isCurrentlyFav) {
                            if (serverAdded) userManager.addFavoriteSong(song.id)
                            else userManager.removeFavoriteSong(song.id)
                        }
                    }

                    is Resource.Failure -> {
                        // API failed: Revert to previous state
                        if (isCurrentlyFav) {
                            userManager.addFavoriteSong(song.id) // Restore favorite status
                        } else {
                            userManager.removeFavoriteSong(song.id) // Restore non-favorite status
                        }
                        Toast.makeText(
                            context,
                            context.getString(R.string.toast_connection_error_with_msg, result.exception.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {}
                }
            }
        }
    }

    // ==================== ALBUM ====================
    fun onAlbumMenuClick(album: Album) {
        MoreOptionsManager.showForAlbum(fragmentManager, album) { action ->
            handleAlbumAction(action, album)
        }
    }

    private fun handleAlbumAction(action: MoreOptionsAction.AlbumAction, album: Album) {
        when (action) {
            is MoreOptionsAction.AlbumAction.PlayAll -> {
                playAlbum(album)
            }

            is MoreOptionsAction.AlbumAction.AddToPlaylist -> {
            }

            is MoreOptionsAction.AlbumAction.GoToArtist -> {
                navigator?.navigatorToDetailScreen(DetailType.Artist(album.artist.id))
            }

            is MoreOptionsAction.AlbumAction.Share -> {
                shareContent(album.title, context.getString(R.string.share_album_message, album.title))
            }
        }
    }

    /**
     * Plays all songs from an album.
     * Loads songs from API if not already available.
     *
     * @param album Album to play
     */
    private fun playAlbum(album: Album) {
        // Album already has song list
        if (album.songs.isNotEmpty()) {
            playerViewModel.setPlaylist(album.songs, 0)
            Toast.makeText(context, context.getString(R.string.toast_playing_album, album.title), Toast.LENGTH_SHORT).show()
            return
        }

        // Load songs from API
        Toast.makeText(context, context.getString(R.string.toast_loading_songs), Toast.LENGTH_SHORT).show()

        scope.launch {
            when (val result = musicRepository.getAlbumById(album.id)) {
                is Resource.Success -> {
                    val fullAlbum = result.result
                    if (fullAlbum.songs.isNotEmpty()) {
                        playerViewModel.setPlaylist(fullAlbum.songs, 0)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Đang phát: ${fullAlbum.title}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Album này chưa có bài hát nào",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                is Resource.Failure -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Lỗi tải Album: ${result.exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                else -> {}
            }
        }
    }

    // ==================== ARTIST ====================
    fun onArtistMenuClick(artist: Artist) {
        val isFollowed = userManager.isArtistFollowed(artist.id)
        MoreOptionsManager.showForArtist(fragmentManager, artist, isFollowed) { action ->
            handleArtistAction(action, artist)
        }
    }

    private fun handleArtistAction(action: MoreOptionsAction.ArtistAction, artist: Artist) {
        when (action) {
            is MoreOptionsAction.ArtistAction.Follow -> toggleFollowArtist(artist)
            is MoreOptionsAction.ArtistAction.Unfollow -> toggleFollowArtist(artist)

            is MoreOptionsAction.ArtistAction.PlayAllSongs -> {
                // Play artist top songs
                playArtist(artist)
            }

            is MoreOptionsAction.ArtistAction.Share -> {
                shareContent(artist.name, context.getString(R.string.share_artist_message, artist.name))
            }
        }
    }

    /**
     * Plays all songs by an artist.
     *
     * @param artist Artist whose songs to play
     */
    private fun playArtist(artist: Artist) {
        Toast.makeText(context, context.getString(R.string.toast_loading_artist_songs, artist.name), Toast.LENGTH_SHORT).show()

        scope.launch {
            val result = musicRepository.getSongsByArtist(artist.id)

            withContext(Dispatchers.Main) {
                when (result) {
                    is Resource.Success -> {
                        val songs = result.result
                        if (songs.isNotEmpty()) {
                            playerViewModel.setPlaylist(songs, 0)
                            Toast.makeText(
                                context,
                                context.getString(R.string.toast_playing_artist, artist.name),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Nghệ sĩ này chưa có bài hát nào",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    is Resource.Failure -> {
                        Toast.makeText(
                            context,
                            "Lỗi tải nhạc: ${result.exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {}
                }
            }
        }
    }

    // ==================== PLAYLIST ====================
    /**
     * Handles playlist menu click.
     *
     * @param playlist Playlist to show options for
     * @param onEditRequest Callback to show edit dialog in Fragment
     * @param onDeleteSuccess Callback when playlist is deleted successfully
     */
    fun onPlaylistMenuClick(
        playlist: Playlist,
        onEditRequest: (Playlist) -> Unit,
        onDeleteSuccess: () -> Unit
    ) {
        MoreOptionsManager.showForPlaylist(fragmentManager, playlist) { action ->
            handlePlaylistAction(action, playlist, onEditRequest, onDeleteSuccess)
        }
    }

    private fun handlePlaylistAction(
        action: MoreOptionsAction.PlaylistAction,
        playlist: Playlist,
        onEditRequest: (Playlist) -> Unit,
        onDeleteSuccess: () -> Unit
    ) {
        when (action) {
            is MoreOptionsAction.PlaylistAction.PlayAll -> {
                if (playlist.songs.isNotEmpty()) {
                    playerViewModel.setPlaylist(playlist.songs, 0)
                    Toast.makeText(context, context.getString(R.string.toast_playing_playlist, playlist.name), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.toast_playlist_empty), Toast.LENGTH_SHORT).show()
                }
            }

            is MoreOptionsAction.PlaylistAction.Edit -> {
                // MediaActionHandler cannot open Gallery, delegate to Fragment
                onEditRequest(playlist)
            }

            is MoreOptionsAction.PlaylistAction.Delete -> {
                showConfirmDeletePlaylist(playlist, onDeleteSuccess)
            }

            is MoreOptionsAction.PlaylistAction.Share -> {
                shareContent(
                    title = "Playlist: ${playlist.name}",
                    message = context.getString(R.string.share_playlist_message, playlist.name)
                )
            }
        }
    }

    /**
     * Shows confirmation dialog for deleting a playlist.
     */
    private fun showConfirmDeletePlaylist(playlist: Playlist, onSuccess: () -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.dialog_delete_playlist_title))
            .setMessage(context.getString(R.string.dialog_delete_playlist_message, playlist.name))
            .setPositiveButton(context.getString(R.string.dialog_delete)) { dialog, _ ->
                dialog.dismiss()
                performDeletePlaylist(playlist.id, onSuccess)
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .show()
    }

    /**
     * Performs playlist deletion via API.
     */
    private fun performDeletePlaylist(playlistId: String, onSuccess: () -> Unit) {
        scope.launch {
            val result = musicRepository.deletePlaylist(playlistId)
            withContext(Dispatchers.Main) {
                if (result is Resource.Success) {
                    Toast.makeText(context, context.getString(R.string.toast_playlist_deleted), Toast.LENGTH_SHORT).show()
                    onSuccess() // Call callback to close Fragment
                } else {
                    Toast.makeText(context, context.getString(R.string.toast_error_with_result, result), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ==================== HELPER: SHARE ====================
    private fun shareContent(title: String, message: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, message)
        }
        // show share dialog
        context.startActivity(
            Intent.createChooser(shareIntent, "Chia sẻ qua").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}