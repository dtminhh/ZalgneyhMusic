package com.example.zalgneyhmusic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.Song

/**
 * Room Entity for Song
 * Stores song information in local database
 */
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val artistImageUrl: String?,
    val albumId: String? = null,
    val albumTitle: String? = null,
    val duration: Int,
    val url: String,
    val imageUrl: String?,
    val lyrics: String? = null,
    val genre: String? = null, // Stored as comma-separated string (e.g., "Pop,Synth-pop")
    val releaseDate: String,
    val plays: Int = 0,
    val likes: Int = 0,
    val isPublic: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    /**
     * Convert Entity to Domain Model
     */
    fun toDomain(): Song {
        return Song(
            id = id,
            title = title,
            artist = Artist(
                id = artistId,
                name = artistName,
                imageUrl = artistImageUrl.orEmpty(),
                bio = null,
                followers = 0,
                verified = false
            ),
            album = albumId?.let {
                Album(
                    id = it,
                    title = albumTitle ?: "",
                    artist = Artist(  // Create Artist object
                        id = artistId,
                        name = artistName,
                        imageUrl = artistImageUrl.orEmpty(),
                        followers = 0,
                        verified = false
                    ),
                    imageUrl = imageUrl.orEmpty()
                )
            },
            duration = duration,
            url = url,
            imageUrl = imageUrl.orEmpty(),
            lyrics = lyrics,
            genre = genre
                ?.split(',')
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() },
            releaseDate = releaseDate,
            plays = plays,
            likes = likes,
            isPublic = isPublic,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        /**
         * Convert Domain Model to Entity
         */
        fun fromDomain(song: Song): SongEntity {
            return SongEntity(
                id = song.id,
                title = song.title,
                artistId = song.artist.id,
                artistName = song.artist.name,
                artistImageUrl = song.artist.imageUrl,
                albumId = song.album?.id,
                albumTitle = song.album?.title,
                duration = song.duration,
                url = song.url,
                imageUrl = song.imageUrl.takeUnless { it.isBlank() },
                lyrics = song.lyrics,
                genre = song.genre
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?.joinToString(","),
                releaseDate = song.releaseDate,
                plays = song.plays,
                likes = song.likes,
                isPublic = song.isPublic,
                createdAt = song.createdAt,
                updatedAt = song.updatedAt
            )
        }
    }
}