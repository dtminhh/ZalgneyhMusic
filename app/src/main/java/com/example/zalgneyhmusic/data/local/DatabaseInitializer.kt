package com.example.zalgneyhmusic.data.local

import com.example.zalgneyhmusic.data.local.entity.AlbumEntity
import com.example.zalgneyhmusic.data.local.entity.SongEntity
import com.example.zalgneyhmusic.data.local.entity.ArtistEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for populate sample data into database
 */
@Singleton
class DatabaseInitializer @Inject constructor(
    private val database: MusicDatabase
) {

    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        // check availble data
        val existingSongs = database.songDao().getAllSongs().first()
        if (existingSongs.isNotEmpty()) {
            return@withContext // Database already have data -> not init again
        }

        // Insert sample artists
        val sampleArtists = listOf(
            ArtistEntity(
                id = "artist_1",
                name = "The Weeknd",
                bio = "Canadian singer and songwriter",
                imageUrl = "https://picsum.photos/200/200?random=1",
                followers = 1500000,
                verified = true
            ),
            ArtistEntity(
                id = "artist_2",
                name = "Taylor Swift",
                bio = "American singer-songwriter",
                imageUrl = "https://picsum.photos/200/200?random=2",
                followers = 2000000,
                verified = true
            ),
            ArtistEntity(
                id = "artist_3",
                name = "Ed Sheeran",
                bio = "English singer-songwriter",
                imageUrl = "https://picsum.photos/200/200?random=3",
                followers = 1800000,
                verified = true
            ),
            ArtistEntity(
                id = "artist_4",
                name = "Billie Eilish",
                bio = "American singer and songwriter",
                imageUrl = "https://picsum.photos/200/200?random=4",
                followers = 1600000,
                verified = true
            ),
            ArtistEntity(
                id = "artist_5",
                name = "Drake",
                bio = "Canadian rapper and singer",
                imageUrl = "https://picsum.photos/200/200?random=5",
                followers = 1900000,
                verified = true
            ),
            ArtistEntity(
                id = "artist_6",
                name = "Ariana Grande",
                bio = "American singer and actress",
                imageUrl = "https://picsum.photos/200/200?random=6",
                followers = 1700000,
                verified = true
            ),
            ArtistEntity(
                id = "artist_7",
                name = "Post Malone",
                bio = "American rapper and singer",
                imageUrl = "https://picsum.photos/200/200?random=7",
                followers = 1400000,
                verified = true
            ),
            ArtistEntity(
                id = "artist_8",
                name = "Dua Lipa",
                bio = "English singer and songwriter",
                imageUrl = "https://picsum.photos/200/200?random=8",
                followers = 1300000,
                verified = true
            )
        )
        database.artistDao().insertArtists(sampleArtists)

        // Insert sample albums
        val sampleAlbums = listOf(
            AlbumEntity(
                id = "album_1",
                title = "After Hours",
                artist = "artist_1",
                releaseYear = 2020,
                imageUrl = "https://picsum.photos/300/300?random=11",
                description = "Fourth studio album",
                totalTracks = 14
            ),
            AlbumEntity(
                id = "album_2",
                title = "Midnights",
                artist = "artist_2",
                releaseYear = 2022,
                imageUrl = "https://picsum.photos/300/300?random=12",
                description = "Tenth studio album",
                totalTracks = 13
            ),
            AlbumEntity(
                id = "album_3",
                title = "÷ (Divide)",
                artist = "artist_3",
                releaseYear = 2017,
                imageUrl = "https://picsum.photos/300/300?random=13",
                description = "Third studio album",
                totalTracks = 16
            ),
            AlbumEntity(
                id = "album_4",
                title = "Happier Than Ever",
                artist = "artist_4",
                releaseYear = 2021,
                imageUrl = "https://picsum.photos/300/300?random=14",
                description = "Second studio album",
                totalTracks = 16
            ),
            AlbumEntity(
                id = "album_5",
                title = "Certified Lover Boy",
                artist = "artist_5",
                releaseYear = 2021,
                imageUrl = "https://picsum.photos/300/300?random=15",
                description = "Sixth studio album",
                totalTracks = 21
            )
        )
        database.albumDao().insertAlbums(sampleAlbums)

        // Insert sample songs
        val sampleSongs = listOf(
            SongEntity(
                id = "song_1",
                title = "Blinding Lights",
                artistId = "artist_1",
                artistName = "The Weeknd",
                artistImageUrl = "https://picsum.photos/200/200?random=1",
                albumId = "album_1",
                albumTitle = "After Hours",
                duration = 200,
                url = "https://example.com/song1.mp3",
                imageUrl = "https://picsum.photos/300/300?random=11",
                genre = "Pop,Synth-pop",
                releaseDate = "2020-11-29",
                plays = 500000,
                likes = 45000
            ),
            SongEntity(
                id = "song_2",
                title = "Save Your Tears",
                artistId = "artist_1",
                artistName = "The Weeknd",
                artistImageUrl = "https://picsum.photos/200/200?random=1",
                albumId = "album_1",
                albumTitle = "After Hours",
                duration = 215,
                url = "https://example.com/song2.mp3",
                imageUrl = "https://picsum.photos/300/300?random=11",
                genre = "Pop,R&B",
                releaseDate = "2020-11-29",
                plays = 480000,
                likes = 42000
            ),
            SongEntity(
                id = "song_3",
                title = "Anti-Hero",
                artistId = "artist_2",
                artistName = "Taylor Swift",
                artistImageUrl = "https://picsum.photos/200/200?random=2",
                albumId = "album_2",
                albumTitle = "Midnights",
                duration = 201,
                url = "https://example.com/song3.mp3",
                imageUrl = "https://picsum.photos/300/300?random=12",
                genre = "Pop,Synth-pop",
                releaseDate = "2022-10-21",
                plays = 520000,
                likes = 48000
            ),
            SongEntity(
                id = "song_4",
                title = "Shape of You",
                artistId = "artist_3",
                artistName = "Ed Sheeran",
                artistImageUrl = "https://picsum.photos/200/200?random=3",
                albumId = "album_3",
                albumTitle = "÷ (Divide)",
                duration = 233,
                url = "https://example.com/song4.mp3",
                imageUrl = "https://picsum.photos/300/300?random=13",
                genre = "Pop,Dancehall",
                releaseDate = "2017-01-06",
                plays = 600000,
                likes = 55000
            ),
            SongEntity(
                id = "song_5",
                title = "Bad Guy",
                artistId = "artist_4",
                artistName = "Billie Eilish",
                artistImageUrl = "https://picsum.photos/200/200?random=4",
                albumId = "album_4",
                albumTitle = "Happier Than Ever",
                duration = 194,
                url = "https://example.com/song5.mp3",
                imageUrl = "https://picsum.photos/300/300?random=14",
                genre = "Pop,Electropop",
                releaseDate = "2019-03-29",
                plays = 550000,
                likes = 50000
            ),
            SongEntity(
                id = "song_6",
                title = "God's Plan",
                artistId = "artist_5",
                artistName = "Drake",
                artistImageUrl = "https://picsum.photos/200/200?random=5",
                albumId = "album_5",
                albumTitle = "Certified Lover Boy",
                duration = 198,
                url = "https://example.com/song6.mp3",
                imageUrl = "https://picsum.photos/300/300?random=15",
                genre = "Hip hop,Trap",
                releaseDate = "2018-01-19",
                plays = 490000,
                likes = 44000
            ),
            SongEntity(
                id = "song_7",
                title = "7 Rings",
                artistId = "artist_6",
                artistName = "Ariana Grande",
                artistImageUrl = "https://picsum.photos/200/200?random=6",
                duration = 178,
                url = "https://example.com/song7.mp3",
                imageUrl = "https://picsum.photos/300/300?random=16",
                genre = "Pop,Trap",
                releaseDate = "2019-01-18",
                plays = 470000,
                likes = 43000
            ),
            SongEntity(
                id = "song_8",
                title = "Circles",
                artistId = "artist_7",
                artistName = "Post Malone",
                artistImageUrl = "https://picsum.photos/200/200?random=7",
                duration = 215,
                url = "https://example.com/song8.mp3",
                imageUrl = "https://picsum.photos/300/300?random=17",
                genre = "Pop,Hip hop",
                releaseDate = "2019-08-30",
                plays = 460000,
                likes = 41000
            ),
            SongEntity(
                id = "song_9",
                title = "Levitating",
                artistId = "artist_8",
                artistName = "Dua Lipa",
                artistImageUrl = "https://picsum.photos/200/200?random=8",
                duration = 203,
                url = "https://example.com/song9.mp3",
                imageUrl = "https://picsum.photos/300/300?random=18",
                genre = "Pop,Disco",
                releaseDate = "2020-03-27",
                plays = 510000,
                likes = 47000
            ),
            SongEntity(
                id = "song_10",
                title = "Starboy",
                artistId = "artist_1",
                artistName = "The Weeknd",
                artistImageUrl = "https://picsum.photos/200/200?random=1",
                duration = 230,
                url = "https://example.com/song10.mp3",
                imageUrl = "https://picsum.photos/300/300?random=19",
                genre = "R&B,Electropop",
                releaseDate = "2016-09-22",
                plays = 580000,
                likes = 53000
            )
        )
        database.songDao().insertSongs(sampleSongs)
    }
}