package com.example.zalgneyhmusic.data.model.domain

import com.google.gson.annotations.SerializedName

/**
 * Domain Model for Song
 * Used in UI, ViewModel, and API response
 */
data class Song(
    @SerializedName("_id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: Artist,
    @SerializedName("album") val album: Album? = null,
    @SerializedName("duration") val duration: Int,
    @SerializedName("fileUrl") val url: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("lyrics") val lyrics: String? = null,
    @SerializedName("genre") val genre: List<String>? = null,
    @SerializedName("releaseDate") val releaseDate: String,
    @SerializedName("plays") val plays: Int = 0,
    @SerializedName("likes") val likes: Int = 0,
    @SerializedName("isPublic") val isPublic: Boolean = true,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

