package com.example.zalgneyhmusic.data.model.domain

import com.google.gson.annotations.SerializedName

/**
 * Domain Model for Artist
 * Used in UI, ViewModel, and API response
 */
data class Artist(
    @SerializedName("_id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("bio")
    val bio: String? = null,

    @SerializedName("imageUrl")
    val imageUrl: String,

    @SerializedName("followers")
    val followers: Int = 0,

    @SerializedName("verified")
    val verified: Boolean = false,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

