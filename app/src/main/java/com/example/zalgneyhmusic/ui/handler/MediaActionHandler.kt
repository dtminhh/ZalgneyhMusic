package com.example.zalgneyhmusic.ui.handler

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.DetailType
import com.example.zalgneyhmusic.data.model.domain.Playlist
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.ui.moreoptions.MoreOptionsAction
import com.example.zalgneyhmusic.ui.moreoptions.MoreOptionsManager
import com.example.zalgneyhmusic.ui.navigation.DetailNavigator
import com.example.zalgneyhmusic.ui.viewmodel.fragment.PlayerViewModel

/**
 * Class that handles ALL menu actions (3 dots) and Play music.
 * Helps Fragment not to repeat handleAction code.
 */
class MediaActionHandler(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val playerViewModel: PlayerViewModel,
    private val navigator: DetailNavigator?
) {

    // ==================== SONG ====================
    fun onSongMenuClick(song: Song) {
        MoreOptionsManager.showForSong(fragmentManager, song) { action ->
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
                // playerViewModel.addToNext(song)
                Toast.makeText(context, "Play next: ${song.title}", Toast.LENGTH_SHORT).show()
            }

            is MoreOptionsAction.SongAction.AddToQueue -> {
                // playerViewModel.addToQueue(song)
                Toast.makeText(context, "Added to queue", Toast.LENGTH_SHORT).show()
            }

            is MoreOptionsAction.SongAction.AddToPlaylist -> {
                // Show Add to playlist dialog
            }

            is MoreOptionsAction.SongAction.GoToArtist -> {
                navigator?.navigatorToDetailScreen(DetailType.Artist(song.artist.id))
            }

            is MoreOptionsAction.SongAction.GoToAlbum -> {
                song.album?.id?.let { albumId ->
                    navigator?.navigatorToDetailScreen(DetailType.Album(albumId))
                } ?: Toast.makeText(context, "Unknown Album", Toast.LENGTH_SHORT).show()
            }

            is MoreOptionsAction.SongAction.Share -> {
                // ShareUtils.shareSong(context, song)
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
                Toast.makeText(context, "Play all: ${album.title}", Toast.LENGTH_SHORT).show()
            }

            is MoreOptionsAction.AlbumAction.AddToPlaylist -> {
            }

            is MoreOptionsAction.AlbumAction.GoToArtist -> {
                navigator?.navigatorToDetailScreen(DetailType.Artist(album.artist.id))
            }

            is MoreOptionsAction.AlbumAction.Share -> {
            }
        }
    }

    // ==================== ARTIST ====================
    fun onArtistMenuClick(artist: Artist) {
        MoreOptionsManager.showForArtist(fragmentManager, artist) { action ->
            handleArtistAction(action, artist)
        }
    }

    private fun handleArtistAction(action: MoreOptionsAction.ArtistAction, artist: Artist) {
        when (action) {
            is MoreOptionsAction.ArtistAction.Follow -> {
                // viewModel.followArtist(artist.id)
                Toast.makeText(context, "Followed ${artist.name}", Toast.LENGTH_SHORT).show()
            }

            is MoreOptionsAction.ArtistAction.PlayAllSongs -> {
                // Play artist top songs
            }

            is MoreOptionsAction.ArtistAction.Share -> {
                // ShareUtils.shareArtist(context, artist)
            }
        }
    }

    // ==================== PLAYLIST ====================
    fun onPlaylistMenuClick(playlist: Playlist) {
        MoreOptionsManager.showForPlaylist(fragmentManager, playlist) { action ->
            handlePlaylistAction(action, playlist)
        }
    }

    private fun handlePlaylistAction(action: MoreOptionsAction.PlaylistAction, playlist: Playlist) {
        when (action) {
            is MoreOptionsAction.PlaylistAction.PlayAll -> { /* Play playlist */
            }

            is MoreOptionsAction.PlaylistAction.Edit -> { /* Open Edit screen */
            }

            is MoreOptionsAction.PlaylistAction.Delete -> { /* Show confirm delete dialog */
            }

            is MoreOptionsAction.PlaylistAction.Share -> { /* Share */
            }
        }
    }
}