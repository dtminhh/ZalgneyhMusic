package com.example.zalgneyhmusic.data.model

import com.google.gson.annotations.SerializedName

data class Playlist(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val songs: List<Song>,
    val isPublic: Boolean,
    val createdBy: String
)

