package com.example.zalgneyhmusic.data.model.api

import com.example.zalgneyhmusic.data.model.domain.Artist
import com.google.gson.annotations.SerializedName

/**
 * DTO for Artist API responses
 * Keep API field mapping decoupled from domain model
 */
data class ArtistDTO(
    @SerializedName("_id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("bio")
    val bio: String? = null,

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

    @SerializedName("followers")
    val followers: Int? = 0,

    @SerializedName("verified")
    val verified: Boolean? = false,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    fun toDomain(): Artist = Artist(
        id = id,
        name = name,
        bio = bio,
        imageUrl = imageUrl ?: "",
        followers = followers ?: 0,
        verified = verified ?: false,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

