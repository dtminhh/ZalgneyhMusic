package com.example.zalgneyhmusic.ui.fragment.mainNav

import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.Song

/**
 * Demo data for testing mini player and player functionality
 */
object DemoData {

    val demoArtist = Artist(
        id = "demo_artist_1",
        name = "Demo Artist",
        bio = "This is a demo artist for testing",
        imageUrl = "https://via.placeholder.com/300",
        followers = 1000,
        verified = false
    )

    val demoAlbum = Album(
        id = "demo_album_1",
        title = "Demo Album",
        artist = "demo_artist_1", // Artist ID as String
        releaseYear = 2024,
        imageUrl = "https://via.placeholder.com/300",
        description = "Demo album for testing",
        totalTracks = 3
    )

    val demoSongs = listOf(
        Song(
            id = "demo_song_1",
            title = "Demo Song 1",
            artist = demoArtist,
            album = demoAlbum,
            duration = 180000, // 3 minutes
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            imageUrl = "https://via.placeholder.com/300",
            lyrics = "Demo lyrics here...",
            genre = listOf("Pop"),
            releaseDate = "2024-01-01",
            plays = 100,
            likes = 50,
            isPublic = true
        ),
        Song(
            id = "demo_song_2",
            title = "Demo Song 2",
            artist = demoArtist,
            album = demoAlbum,
            duration = 240000, // 4 minutes
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            imageUrl = "https://via.placeholder.com/300",
            lyrics = "Demo lyrics here...",
            genre = listOf("Rock"),
            releaseDate = "2024-01-02",
            plays = 200,
            likes = 75,
            isPublic = true
        ),
        Song(
            id = "demo_song_3",
            title = "Demo Song 3",
            artist = demoArtist,
            album = demoAlbum,
            duration = 200000, // 3:20 minutes
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            imageUrl = "https://via.placeholder.com/300",
            lyrics = "Demo lyrics here...",
            genre = listOf("Pop", "Rock"),
            releaseDate = "2024-01-03",
            plays = 150,
            likes = 60,
            isPublic = true
        )
    )
}
