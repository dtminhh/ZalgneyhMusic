package com.example.zalgneyhmusic.data.model

import com.google.gson.annotations.SerializedName

data class Song(
    @SerializedName("_id")
    val id: String,
    val title: String,
    val artist: Artist,
    val album: String?,
    val duration: Int,
    val fileUrl: String,
    val imageUrl: String?,
    val genre: List<String>,
    val plays: Int,
    val likes: Int,
    val releaseDate: String,
    val createdAt: String
)

