package com.example.zalgneyhmusic.data.model.domain

/**
 * Album Domain Model
 * For UI, ViewModel and API response
 */
data class Album(
    val id: String,
    val title: String,
    val artist: Artist,  // artist as domain model
    val releaseYear: Int? = null,
    val coverImage: String? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val totalTracks: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    // Unified image accessor used by UI (prefers coverImage then imageUrl)
    val image: String?
        get() = coverImage ?: imageUrl
}
