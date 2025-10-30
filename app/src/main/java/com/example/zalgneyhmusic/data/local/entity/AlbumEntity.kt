package com.example.zalgneyhmusic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.zalgneyhmusic.data.model.domain.Album

/**
 * Room Entity for Album
 * Stores album information in local database
 */
@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val releaseYear: Int? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val totalTracks: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): Album = Album(
        id = id,
        title = title,
        artist = artist,
        releaseYear = releaseYear,
        imageUrl = imageUrl,
        description = description,
        totalTracks = totalTracks,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(album: Album): AlbumEntity = AlbumEntity(
            id = album.id,
            title = album.title,
            artist = album.artist,
            releaseYear = album.releaseYear,
            imageUrl = album.imageUrl,
            description = album.description,
            totalTracks = album.totalTracks,
            createdAt = album.createdAt,
            updatedAt = album.updatedAt
        )
    }
}