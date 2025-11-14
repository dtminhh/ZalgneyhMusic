package com.example.zalgneyhmusic.data.model.api

import com.example.zalgneyhmusic.data.model.domain.Song
import com.google.gson.annotations.SerializedName

/**
 * DTO for Song API Response
 * Matches backend API format exactly:
 * - artist: Array of Artist objects
 * - album: Single Album object (nullable, populated by backend)
 */
data class SongDTO(
    @SerializedName("_id")
    val id: String,

    @SerializedName("title")
    val title: String,

    // Backend returns artist as ARRAY of Artist objects
    @SerializedName("artist")
    val artist: List<ArtistDTO>,

    // Backend returns album as Object (populated), nullable
    @SerializedName("album")
    val album: AlbumDTO? = null,

    @SerializedName("duration")
    val duration: Int, // in seconds

    @SerializedName("fileUrl")
    val fileUrl: String,

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

    @SerializedName("lyrics")
    val lyrics: String? = null,

    // Backend returns genre as ARRAY of strings
    @SerializedName("genre")
    val genre: List<String>? = null,

    @SerializedName("releaseDate")
    val releaseDate: String? = null,

    @SerializedName("plays")
    val plays: Int = 0,

    @SerializedName("likes")
    val likes: Int? = null,

    // Nullable to avoid Gson defaulting missing boolean to false; we default to true downstream
    @SerializedName("isPublic")
    val isPublic: Boolean? = true,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    /**
     * Convert DTO to Domain model
     * Uses first artist from array as primary artist
     */
    fun toDomain(): Song {
        val primaryArtist = artist.firstOrNull()?.toDomain()
            ?: com.example.zalgneyhmusic.data.model.domain.Artist(
                id = "",
                name = "Unknown Artist",
                imageUrl = "",
                followers = 0,
                verified = false
            )

        return Song(
            id = id,
            title = title,
            artist = primaryArtist,
            album = album?.toDomain(),
            duration = duration,
            url = fileUrl,
            imageUrl = imageUrl ?: "",
            lyrics = lyrics,
            genre = genre,
            releaseDate = releaseDate ?: "",
            plays = plays,
            likes = likes ?: 0,
            isPublic = isPublic ?: true,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
