package com.example.zalgneyhmusic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.zalgneyhmusic.data.model.domain.Artist

/**
 * Room Entity for Artist
 * Storage artists information in local database
 */
@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val bio: String? = null,
    val imageUrl: String,
    val followers: Int = 0,
    val verified: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): Artist = Artist(
        id = id,
        name = name,
        bio = bio,
        imageUrl = imageUrl,
        followers = followers,
        verified = verified,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(artist: Artist): ArtistEntity = ArtistEntity(
            id = artist.id,
            name = artist.name,
            bio = artist.bio,
            imageUrl = artist.imageUrl,
            followers = artist.followers,
            verified = artist.verified,
            createdAt = artist.createdAt,
            updatedAt = artist.updatedAt
        )
    }
}