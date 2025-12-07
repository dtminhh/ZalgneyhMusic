package com.example.zalgneyhmusic.data.repository.music

import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.Playlist
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.ui.viewmodel.fragment.SearchResults
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining methods for Music Repository
 * Enables easy switching between Local Database and API
 */
interface MusicRepository {
    // Songs

    suspend fun downloadSong(songId: String): Resource<Boolean>
    suspend fun removeDownloadedSong(songId: String): Resource<Boolean>
    fun getDownloadedSongs(): Flow<List<Song>>
    fun getAllSongs(): Flow<Resource<List<Song>>>
    fun getTopSongs(limit: Int = 10): Flow<Resource<List<Song>>>
    fun getRecentSongs(limit: Int = 10): Flow<Resource<List<Song>>>
    fun getNewSongs(limit: Int = 10): Flow<Resource<List<Song>>>
    suspend fun getSongById(id: String): Resource<Song>
    fun searchSongs(query: String): Flow<Resource<List<Song>>>

    suspend fun addToRecentlyPlayed(song: Song)

    fun getListeningHistory(): Flow<List<Song>>

    fun getPersonalizedSuggestions(): Flow<Resource<List<Song>>>

    suspend fun toggleFavorite(playlistId: String, songId: String): Resource<Boolean>

    // Artists
    fun getAllArtists(): Flow<Resource<List<Artist>>>
    fun getTopArtists(limit: Int = 10): Flow<Resource<List<Artist>>>
    suspend fun getArtistById(id: String): Resource<Artist>
    suspend fun getSongsByArtist(artistId: String): Resource<List<Song>>
    suspend fun getAlbumsByArtist(artistId: String): Resource<List<Album>>

    suspend fun getFollowedArtists(): Resource<List<Artist>>

    suspend fun toggleFollowArtist(artistId: String): Resource<Boolean>

    // Albums
    fun getAllAlbums(): Flow<Resource<List<Album>>>
    fun getRecentAlbums(limit: Int = 10): Flow<Resource<List<Album>>>
    suspend fun getAlbumById(id: String): Resource<Album>

    suspend fun createPlaylist(
        name: String,
        description: String?,
        imageFile: java.io.File?
    ): Resource<Playlist>

    suspend fun getMyPlaylists(): Resource<List<Playlist>>
    suspend fun addSongToPlaylist(playlistId: String, songId: String): Resource<Any>

    suspend fun deletePlaylist(id: String): Resource<Boolean>

    suspend fun updatePlaylist(
        id: String,
        name: String,
        imageFile: java.io.File?
    ): Resource<Playlist>

    fun searchEverything(query: String): Flow<Resource<SearchResults>>
}