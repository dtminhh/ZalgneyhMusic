package com.example.zalgneyhmusic.data.model.domain

import com.google.gson.annotations.SerializedName

/**
 * Album Domain Model
 * For UI, ViewModel and API response
 */
data class Album(
    @SerializedName("_id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("artist")
    val artist: String,

    @SerializedName("releaseYear")
    val releaseYear: Int? = null,

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
)

