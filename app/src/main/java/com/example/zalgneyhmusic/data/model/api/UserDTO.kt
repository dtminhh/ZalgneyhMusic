package com.example.zalgneyhmusic.data.model.api

import com.example.zalgneyhmusic.data.model.domain.User
import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for User API responses.
 * Maps the backend JSON structure to the [User] domain model.
 */
data class UserDTO(
    @SerializedName("_id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("role") val role: String,
    @SerializedName("favoritePlaylist") val favoritePlaylist: String? ,// Backend returns playlist ID
    @SerializedName("photoUrl")
    val photoUrl: String? = null
) {
    fun toDomain() = User(
        id = id,
        email = email,
        displayName = displayName ?: "User",
        role = role,
        photoUrl = photoUrl,
        favoritePlaylistId = favoritePlaylist
    )
}