package com.example.zalgneyhmusic.ui.moreoptions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.zalgneyhmusic.R

/**
 * Type-safe actions for More Options menu
 */
sealed class MoreOptionsAction(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int
) {

    sealed class SongAction(titleRes: Int, iconRes: Int) : MoreOptionsAction(titleRes, iconRes) {
        object PlayNext : SongAction(R.string.mo_play_next, R.drawable.ic_play)
        object AddToQueue : SongAction(R.string.mo_add_to_queue, R.drawable.ic_playlist_placeholder)
        object AddToPlaylist : SongAction(R.string.mo_add_to_playlist, R.drawable.ic_playlist_placeholder)
        object AddToFavorite : SongAction(R.string.favorite, R.drawable.ic_favorite)
        object Download : SongAction(R.string.download, R.drawable.ic_download)
        object RemoveFromFavorites : SongAction(R.string.un_favorite, R.drawable.ic_favorite_on)
        object GoToArtist : SongAction(R.string.mo_go_to_artist, R.drawable.ic_person)
        object GoToAlbum : SongAction(R.string.mo_go_to_album, R.drawable.ic_album)
        object Share : SongAction(R.string.mo_share, R.drawable.ic_share)
    }

    sealed class ArtistAction(titleRes: Int, iconRes: Int) : MoreOptionsAction(titleRes, iconRes) {
        object Follow : ArtistAction(R.string.mo_follow, R.drawable.ic_person)

        object Unfollow : ArtistAction(R.string.mo_unfollow, R.drawable.ic_person)
        object PlayAllSongs : ArtistAction(R.string.mo_play_all_songs, R.drawable.ic_play)
        object Share : ArtistAction(R.string.mo_share, R.drawable.ic_share)
    }

    sealed class AlbumAction(titleRes: Int, iconRes: Int) : MoreOptionsAction(titleRes, iconRes) {
        object PlayAll : AlbumAction(R.string.mo_play_all, R.drawable.ic_play)
        object AddToPlaylist : AlbumAction(R.string.mo_add_all_to_playlist, R.drawable.ic_playlist_placeholder)
        object GoToArtist : AlbumAction(R.string.mo_go_to_artist, R.drawable.ic_person)
        object Share : AlbumAction(R.string.mo_share, R.drawable.ic_share)
    }

    sealed class PlaylistAction(titleRes: Int, iconRes: Int) : MoreOptionsAction(titleRes, iconRes) {
        object PlayAll : PlaylistAction(R.string.mo_play_all, R.drawable.ic_play)
        object Edit : PlaylistAction(R.string.mo_edit_playlist, R.drawable.ic_edit)
        object Delete : PlaylistAction(R.string.mo_delete_playlist, R.drawable.ic_close)
        object Share : PlaylistAction(R.string.mo_share, R.drawable.ic_share)
    }
}

