package com.example.zalgneyhmusic.data.model

data class SongResponse(
    val success: Boolean,
    val data: List<Song>,
    val pagination: Pagination? = null
)

data class SingleSongResponse(
    val success: Boolean,
    val data: Song
)

data class ArtistResponse(
    val success: Boolean,
    val data: List<Artist>
)

data class SingleArtistResponse(
    val success: Boolean,
    val data: Artist
)

data class PlaylistResponse(
    val success: Boolean,
    val data: List<Playlist>
)

data class SinglePlaylistResponse(
    val success: Boolean,
    val data: Playlist
)

data class Pagination(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val itemsPerPage: Int
)