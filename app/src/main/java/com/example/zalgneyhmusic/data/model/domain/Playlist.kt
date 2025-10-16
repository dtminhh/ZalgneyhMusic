package com.example.zalgneyhmusic.data.model.domain

import com.google.gson.annotations.SerializedName

/**
 * Domain Model cho Playlist
 * use in UI, ViewModel and API response
 */
data class Playlist(
    @SerializedName("_id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("imageUrl")
    val imageUrl: String,

    @SerializedName("songs")
    val songs: List<String> = emptyList(), // List song IDs

    @SerializedName("isPublic")
    val isPublic: Boolean = true,

    @SerializedName("createdBy")
    val createdBy: String = "Admin",

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
