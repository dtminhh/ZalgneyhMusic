package com.example.zalgneyhmusic.data.model

import com.google.gson.annotations.SerializedName

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
