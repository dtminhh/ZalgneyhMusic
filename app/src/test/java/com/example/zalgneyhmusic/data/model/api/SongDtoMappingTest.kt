package com.example.zalgneyhmusic.data.model.api

import com.example.zalgneyhmusic.data.local.entity.SongEntity
import com.example.zalgneyhmusic.data.model.domain.Song
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SongDtoMappingTest {

    private fun gson() = GsonBuilder()
        .registerTypeAdapter(AlbumDTO::class.java, AlbumDeserializer())
        .registerTypeAdapter(PlaylistDTO::class.java, PlaylistDeserializer())
        .create()

    @Test
    fun `SongDTO toDomain maps fields correctly`() {
        val json = """
            {
              "_id": "song_100",
              "title": "Test Song",
              "artist": [ { "_id": "artist_100", "name": "Tester" } ],
              "album": { "_id": "album_100", "title": "Test Album", "artist": { "_id": "artist_100", "name": "Tester" }, "totalTracks": 10 },
              "duration": 180,
              "fileUrl": "https://example.com/audio.mp3",
              "imageUrl": "https://example.com/image.jpg",
              "lyrics": "la la la",
              "genre": ["Pop", "Synth-pop"],
              "releaseDate": "2024-01-01",
              "plays": 5,
              "likes": 2,
              "isPublic": true
            }
        """.trimIndent()

        val dto = gson().fromJson(json, SongDTO::class.java)
        val domain: Song = dto.toDomain()

        assertEquals("song_100", domain.id)
        assertEquals("Test Song", domain.title)
        assertEquals("Tester", domain.artist.name)
        assertNotNull(domain.album)
        assertEquals("album_100", domain.album?.id)
        assertEquals(180, domain.duration)
        assertEquals("https://example.com/audio.mp3", domain.url)
        assertEquals("https://example.com/image.jpg", domain.imageUrl)
        assertEquals(listOf("Pop", "Synth-pop"), domain.genre)
        assertEquals("2024-01-01", domain.releaseDate)
        assertEquals(5, domain.plays)
        assertEquals(2, domain.likes)
        assertEquals(true, domain.isPublic)
    }

    @Test
    fun `SongEntity maps genre string to list and back`() {
        val entity = SongEntity(
            id = "song_200",
            title = "Roundtrip",
            artistId = "artist_200",
            artistName = "Artist 200",
            artistImageUrl = "",
            albumId = null,
            albumTitle = null,
            duration = 120,
            url = "u",
            imageUrl = "i",
            lyrics = null,
            genre = "Pop, Synth-pop ,",
            releaseDate = "2024-01-01",
            plays = 0,
            likes = 0,
            isPublic = true
        )

        val domain = entity.toDomain()
        assertEquals(listOf("Pop", "Synth-pop"), domain.genre)

        val entity2 = SongEntity.fromDomain(domain)
        assertEquals("Pop,Synth-pop", entity2.genre)
    }
}

