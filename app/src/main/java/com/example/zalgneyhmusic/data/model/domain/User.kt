package com.example.zalgneyhmusic.data.model.domain

/**
 * Domain model representing a synchronized user.
 * Contains backend-specific fields like MongoDB ID and role.
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val role: String,
    val photoUrl: String? = null,
    val favoritePlaylistId: String?
)