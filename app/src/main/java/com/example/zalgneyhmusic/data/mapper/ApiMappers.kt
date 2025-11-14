package com.example.zalgneyhmusic.data.mapper

import com.example.zalgneyhmusic.data.local.entity.AlbumEntity
import com.example.zalgneyhmusic.data.local.entity.ArtistEntity
import com.example.zalgneyhmusic.data.local.entity.SongEntity
import com.example.zalgneyhmusic.data.model.api.AlbumDTO
import com.example.zalgneyhmusic.data.model.api.ArtistDTO
import com.example.zalgneyhmusic.data.model.api.SongDTO

/**
 * API DTO -> Room Entity mappers
 * Keep all API-specific shapes here (flatten relationships, join list fields, etc.)
 */

fun ArtistDTO.toEntity(): ArtistEntity = ArtistEntity(
    id = id,
    name = name,
    bio = bio,
    imageUrl = imageUrl ?: "",
    followers = followers ?: 0,
    verified = verified ?: false,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AlbumDTO.toEntity(): AlbumEntity = AlbumEntity(
    id = id,
    title = title,
    artistId = artist.id,
    artistName = artist.name,
    releaseYear = releaseYear,
    imageUrl = imageUrl,
    coverImage = coverImage,
    description = description,
    totalTracks = totalTracks,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SongDTO.toEntity(): SongEntity {
    val primaryArtist = artist.firstOrNull()
    return SongEntity(
        id = id,
        title = title,
        artistId = primaryArtist?.id ?: "",
        artistName = primaryArtist?.name ?: "Unknown Artist",
        artistImageUrl = primaryArtist?.imageUrl ?: "",
        albumId = album?.id,
        albumTitle = album?.title,
        duration = duration,
        url = fileUrl,
        imageUrl = imageUrl ?: "",
        lyrics = lyrics,
        genre = genre?.map { it.trim() }?.filter { it.isNotEmpty() }?.joinToString(","),
        releaseDate = releaseDate ?: "",
        plays = plays,
        likes = likes ?: 0,
        isPublic = isPublic ?: true,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
