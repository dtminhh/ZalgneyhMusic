package com.example.zalgneyhmusic.ui.moreoptions

import androidx.fragment.app.FragmentManager
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.Playlist
import com.example.zalgneyhmusic.data.model.domain.Song

/**
 * Simple API to show More Options menu
 * Usage: MoreOptionsManager.showForSong(fragmentManager, song) { action -> ... }
 */
object MoreOptionsManager {

    fun showForSong(
        fragmentManager: FragmentManager,
        song: Song,
        onActionClick: (MoreOptionsAction.SongAction) -> Unit
    ) {
        MoreOptionsBottomSheet.forSong(
            title = song.title,
            subtitle = song.artist.name,
            imageUrl = song.imageUrl,
            actions = listOf(
                MoreOptionsAction.SongAction.PlayNext,
                MoreOptionsAction.SongAction.AddToQueue,
                MoreOptionsAction.SongAction.AddToPlaylist,
                MoreOptionsAction.SongAction.GoToArtist,
                MoreOptionsAction.SongAction.GoToAlbum,
                MoreOptionsAction.SongAction.Share
            ),
            onActionSelected = onActionClick
        ).show(fragmentManager, "SongOptions")
    }

    fun showForArtist(
        fragmentManager: FragmentManager,
        artist: Artist,
        onActionClick: (MoreOptionsAction.ArtistAction) -> Unit
    ) {
        MoreOptionsBottomSheet.forArtist(
            title = artist.name,
            subtitle = "${artist.followers} followers",
            imageUrl = artist.imageUrl,
            actions = listOf(
                MoreOptionsAction.ArtistAction.Follow,
                MoreOptionsAction.ArtistAction.PlayAllSongs,
                MoreOptionsAction.ArtistAction.Share
            ),
            onActionSelected = onActionClick
        ).show(fragmentManager, "ArtistOptions")
    }

    fun showForAlbum(
        fragmentManager: FragmentManager,
        album: Album,
        onActionClick: (MoreOptionsAction.AlbumAction) -> Unit
    ) {
        MoreOptionsBottomSheet.forAlbum(
            title = album.title,
            subtitle = album.artist.name,
            imageUrl = album.image ?: "",
            actions = listOf(
                MoreOptionsAction.AlbumAction.PlayAll,
                MoreOptionsAction.AlbumAction.AddToPlaylist,
                MoreOptionsAction.AlbumAction.GoToArtist,
                MoreOptionsAction.AlbumAction.Share
            ),
            onActionSelected = onActionClick
        ).show(fragmentManager, "AlbumOptions")
    }

    fun showForPlaylist(
        fragmentManager: FragmentManager,
        playlist: Playlist,
        onActionClick: (MoreOptionsAction.PlaylistAction) -> Unit
    ) {
        MoreOptionsBottomSheet.forPlaylist(
            title = playlist.name,
            subtitle = "${playlist.songs.size} songs",
            imageUrl = playlist.imageUrl,
            actions = listOf(
                MoreOptionsAction.PlaylistAction.PlayAll,
                MoreOptionsAction.PlaylistAction.Edit,
                MoreOptionsAction.PlaylistAction.Delete,
                MoreOptionsAction.PlaylistAction.Share
            ),
            onActionSelected = onActionClick
        ).show(fragmentManager, "PlaylistOptions")
    }
}
