package com.example.zalgneyhmusic.data.model

import com.google.gson.annotations.SerializedName

data class Artist(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val bio: String?,
    val imageUrl: String?,
    val followers: Int,
    val verified: Boolean
)