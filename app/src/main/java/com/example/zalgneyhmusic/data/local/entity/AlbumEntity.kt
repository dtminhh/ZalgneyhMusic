package com.example.zalgneyhmusic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist

/**
 * Room Entity for Album
 * Stores album information in local database
 * Note: Artist object is flattened to store only ID and name
 */
@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artistId: String,  // Store artist ID
    val artistName: String,  // Store artist name
    val releaseYear: Int? = null,
    val imageUrl: String? = null,
    val coverImage: String? = null,
    val description: String? = null,
    val totalTracks: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): Album = Album(
        id = id,
        title = title,
        artist = Artist(  // Recreate Artist object with minimal data
            id = artistId,
            name = artistName,
            imageUrl = "", // Will be fetched separately if needed
            followers = 0,
            verified = false
        ),
        releaseYear = releaseYear,
        imageUrl = imageUrl,
        coverImage = coverImage,
        description = description,
        totalTracks = totalTracks,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(album: Album): AlbumEntity = AlbumEntity(
            id = album.id,
            title = album.title,
            artistId = album.artist.id,  // Extract artist ID
            artistName = album.artist.name,  // Extract artist name
            releaseYear = album.releaseYear,
            imageUrl = album.imageUrl,
            coverImage = album.coverImage,
            description = album.description,
            totalTracks = album.totalTracks,
            createdAt = album.createdAt,
            updatedAt = album.updatedAt
        )
    }
}