package com.example.zalgneyhmusic.data.model.api

import com.example.zalgneyhmusic.data.model.domain.Album
import com.google.gson.annotations.SerializedName

/**
 * DTO for Album API responses
 * Keeps API mapping decoupled from domain model
 */
data class AlbumDTO(
    @SerializedName("_id")
    val id: String,

    @SerializedName("title")
    val title: String,

    // Backend may send artist as string (id) or object; handled by custom deserializer
    @SerializedName("artist")
    val artist: ArtistDTO,

    @SerializedName("releaseYear")
    val releaseYear: Int? = null,

    @SerializedName("coverImage")
    val coverImage: String? = null,

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("totalTracks")
    val totalTracks: Int = 0,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    fun toDomain(): Album = Album(
        id = id,
        title = title,
        artist = artist.toDomain(),
        releaseYear = releaseYear,
        coverImage = coverImage,
        imageUrl = imageUrl,
        description = description,
        totalTracks = totalTracks,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

