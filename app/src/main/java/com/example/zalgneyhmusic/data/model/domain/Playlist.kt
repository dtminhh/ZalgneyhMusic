package com.example.zalgneyhmusic.data.model.domain

/**
 * Domain Model cho Playlist
 * use in UI, ViewModel and business logic
 */
data class Playlist(
    val id: String,
    val name: String,
    val description: String? = null,
    val imageUrl: String,
    val songs: List<String> = emptyList(), // List song IDs
    val isPublic: Boolean = true,
    val createdBy: String = "Admin",
    val createdAt: String? = null,
    val updatedAt: String? = null
)
