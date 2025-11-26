package com.example.zalgneyhmusic.data.model.api

import com.google.gson.annotations.SerializedName

data class SearchResponseDTO(
    @SerializedName("songs") val songs: List<SongDTO> = emptyList(),
    @SerializedName("artists") val artists: List<ArtistDTO> = emptyList(),
    @SerializedName("albums") val albums: List<AlbumDTO> = emptyList()
)