package com.example.zalgneyhmusic.data.model.domain

/**
 * Domain Model for Artist
 * Used in UI, ViewModel, and business logic (decoupled from API serialization)
 */
data class Artist(
    val id: String,
    val name: String,
    val bio: String? = null,
    val imageUrl: String,
    val followers: Int = 0,
    val verified: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
