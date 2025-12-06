package com.example.zalgneyhmusic.data.model.api

import com.example.zalgneyhmusic.data.model.domain.Playlist
import com.google.gson.annotations.SerializedName

/**
 * DTO for Playlist API responses
 * Keeps API mapping decoupled from domain model
 */
data class PlaylistDTO(
    @SerializedName("_id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("songs")
    val songs: List<SongDTO> = emptyList(),

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

    @SerializedName("isPublic")
    val isPublic: Boolean = true,

    @SerializedName("createdBy")
    val createdBy: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    fun toDomain(): Playlist = Playlist(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl ?: "",
        songs = songs.map { it.toDomain() },
        isPublic = isPublic,
        createdBy = createdBy ?: "Admin",
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

