package com.example.zalgneyhmusic.data.mapper

import com.example.zalgneyhmusic.data.local.entity.AlbumEntity
import com.example.zalgneyhmusic.data.local.entity.ArtistEntity
import com.example.zalgneyhmusic.data.local.entity.SongEntity
import com.example.zalgneyhmusic.data.model.api.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.*
import org.junit.Test

class ApiMappersTest {

    private fun gson(): Gson = GsonBuilder()
        .registerTypeAdapter(AlbumDTO::class.java, AlbumDeserializer())
        .registerTypeAdapter(PlaylistDTO::class.java, PlaylistDeserializer())
        .create()

    @Test
    fun `ArtistDTO toEntity maps fields and defaults`() {
        val dto = ArtistDTO(
            id = "artist_1",
            name = "Artist One",
            bio = null,
            imageUrl = null,
            followers = null,
            verified = null,
            createdAt = "2024-01-01",
            updatedAt = "2024-01-02"
        )

        val entity: ArtistEntity = dto.toEntity()
        assertEquals("artist_1", entity.id)
        assertEquals("Artist One", entity.name)
        assertNull(entity.bio)
        assertEquals("", entity.imageUrl) // default
        assertEquals(0, entity.followers) // default
        assertFalse(entity.verified) // default
        assertEquals("2024-01-01", entity.createdAt)
        assertEquals("2024-01-02", entity.updatedAt)
    }

    @Test
    fun `AlbumDTO toEntity flattens artist and keeps optional fields`() {
        val json = """
            {
              "_id": "album_10",
              "title": "Album Ten",
              "artist": { "_id": "artist_10", "name": "Artist Ten" },
              "releaseYear": 2020,
              "coverImage": "https://img/cover.jpg",
              "imageUrl": "https://img/image.jpg",
              "description": "Desc",
              "totalTracks": 8,
              "createdAt": "2024-01-01",
              "updatedAt": "2024-01-02"
            }
        """.trimIndent()
        val dto = gson().fromJson(json, AlbumDTO::class.java)

        val entity: AlbumEntity = dto.toEntity()
        assertEquals("album_10", entity.id)
        assertEquals("Album Ten", entity.title)
        assertEquals("artist_10", entity.artistId)
        assertEquals("Artist Ten", entity.artistName)
        assertEquals(2020, entity.releaseYear)
        assertEquals("https://img/image.jpg", entity.imageUrl)
        assertEquals("https://img/cover.jpg", entity.coverImage)
        assertEquals("Desc", entity.description)
        assertEquals(8, entity.totalTracks)
        assertEquals("2024-01-01", entity.createdAt)
        assertEquals("2024-01-02", entity.updatedAt)
    }

    @Test
    fun `SongDTO toEntity maps primary artist album and genre`() {
        val json = """
            {
              "_id": "song_1",
              "title": "Song One",
              "artist": [
                { "_id": "artist_1", "name": "Artist One", "imageUrl": "https://img/a1.jpg" },
                { "_id": "artist_2", "name": "Artist Two" }
              ],
              "album": { "_id": "album_1", "title": "Album One", "artist": {"_id":"artist_1","name":"Artist One"} },
              "duration": 200,
              "fileUrl": "https://media/song1.mp3",
              "imageUrl": "https://img/s1.jpg",
              "lyrics": "la la",
              "genre": ["Pop", " Synth-pop ", ""],
              "releaseDate": "2024-05-01",
              "plays": 3,
              "likes": 2,
              "isPublic": true
            }
        """.trimIndent()

        val dto = gson().fromJson(json, SongDTO::class.java)
        val entity: SongEntity = dto.toEntity()

        assertEquals("song_1", entity.id)
        assertEquals("Song One", entity.title)
        assertEquals("artist_1", entity.artistId)
        assertEquals("Artist One", entity.artistName)
        assertEquals("https://img/a1.jpg", entity.artistImageUrl)
        assertEquals("album_1", entity.albumId)
        assertEquals("Album One", entity.albumTitle)
        assertEquals(200, entity.duration)
        assertEquals("https://media/song1.mp3", entity.url)
        assertEquals("https://img/s1.jpg", entity.imageUrl)
        assertEquals("la la", entity.lyrics)
        assertEquals("Pop,Synth-pop", entity.genre) // joined and trimmed
        assertEquals("2024-05-01", entity.releaseDate)
        assertEquals(3, entity.plays)
        assertEquals(2, entity.likes)
        assertTrue(entity.isPublic)
    }

    @Test
    fun `SongDTO toEntity handles missing fields with safe defaults`() {
        val json = """
            {
              "_id": "song_2",
              "title": "No Album No Artist",
              "artist": [],
              "duration": 0,
              "fileUrl": "u"
            }
        """.trimIndent()

        val dto = gson().fromJson(json, SongDTO::class.java)
        val entity = dto.toEntity()

        assertEquals("song_2", entity.id)
        assertEquals("No Album No Artist", entity.title)
        assertEquals("", entity.artistId) // fallback
        assertEquals("Unknown Artist", entity.artistName) // fallback
        assertEquals("", entity.artistImageUrl) // fallback
        assertNull(entity.albumId)
        assertNull(entity.albumTitle)
        assertEquals(0, entity.duration)
        assertEquals("u", entity.url)
        assertEquals("", entity.imageUrl) // default when null
        assertNull(entity.lyrics)
        assertNull(entity.genre)
        assertEquals("", entity.releaseDate) // default when null
        assertEquals(0, entity.plays)
        assertEquals(0, entity.likes)
        assertTrue(entity.isPublic)
    }
}

