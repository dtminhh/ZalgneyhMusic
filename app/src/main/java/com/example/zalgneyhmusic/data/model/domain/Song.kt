package com.example.zalgneyhmusic.data.model.domain

/**
 * Domain Model for Song
 * Used in UI, ViewModel, and business logic
 */
data class Song(
    val id: String,
    val title: String,
    val artist: Artist,
    val album: Album? = null,
    val duration: Int,
    val url: String,
    val imageUrl: String,
    val lyrics: String? = null,
    val genre: List<String>? = null,
    val releaseDate: String,
    val plays: Int = 0,
    val likes: Int = 0,
    val isPublic: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
