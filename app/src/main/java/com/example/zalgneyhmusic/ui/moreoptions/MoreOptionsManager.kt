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
        isFavorite: Boolean,
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
                MoreOptionsAction.SongAction.Download,
                if (isFavorite) {
                    MoreOptionsAction.SongAction.RemoveFromFavorites
                } else {
                    MoreOptionsAction.SongAction.AddToFavorite
                },
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
        isFollowed: Boolean,
        onActionClick: (MoreOptionsAction.ArtistAction) -> Unit
    ) {
        MoreOptionsBottomSheet.forArtist(
            title = artist.name,
            subtitle = "${artist.followers} followers",
            imageUrl = artist.imageUrl,
            actions = listOf(
                if (isFollowed) {
                    MoreOptionsAction.ArtistAction.Unfollow
                } else {
                    MoreOptionsAction.ArtistAction.Follow
                },
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

    /**
     * Shows more options bottom sheet for a playlist.
     * Actions are filtered based on whether the playlist is a default (favorites) playlist.
     *
     * @param fragmentManager Fragment manager to show the bottom sheet
     * @param playlist Playlist to show options for
     * @param onActionClick Callback for action clicks
     */
    fun showForPlaylist(
        fragmentManager: FragmentManager,
        playlist: Playlist,
        onActionClick: (MoreOptionsAction.PlaylistAction) -> Unit
    ) {
        // Filter actions based on playlist type
        val actions = if (playlist.isDefault) {
            // Default playlist (Favorites): Only allow Play All and Share
            listOf(
                MoreOptionsAction.PlaylistAction.PlayAll,
                MoreOptionsAction.PlaylistAction.Share
            )
        } else {
            // Custom playlist: Allow full actions (Edit, Delete, etc.)
            listOf(
                MoreOptionsAction.PlaylistAction.PlayAll,
                MoreOptionsAction.PlaylistAction.Edit,
                MoreOptionsAction.PlaylistAction.Delete,
                MoreOptionsAction.PlaylistAction.Share
            )
        }

        MoreOptionsBottomSheet.forPlaylist(
            title = playlist.name,
            subtitle = "${playlist.songs.size} songs",
            imageUrl = playlist.imageUrl,
            actions = actions,
            onActionSelected = onActionClick
        ).show(fragmentManager, "PlaylistOptions")
    }
}
