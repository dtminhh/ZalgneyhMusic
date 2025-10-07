package com.example.zalgneyhmusic.data.api

import com.example.zalgneyhmusic.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface MusicApiService {

    // ===== SONGS =====

    @GET("api/songs")
    suspend fun getAllSongs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): SongResponse

    @GET("api/songs/{id}")
    suspend fun getSongById(@Path("id") id: String): SingleSongResponse

    @GET("api/songs/search")
    suspend fun searchSongs(@Query("q") query: String): SongResponse

    @GET("api/songs/trending")
    suspend fun getTrendingSongs(@Query("limit") limit: Int = 10): SongResponse

    @GET("api/songs/new")
    suspend fun getNewSongs(@Query("limit") limit: Int = 10): SongResponse

    @GET("api/songs/genre/{genre}")
    suspend fun getSongsByGenre(
        @Path("genre") genre: String,
        @Query("limit") limit: Int = 20
    ): SongResponse

    // Stream URL: BASE_URL/api/songs/stream/{id}
    // ExoPlayer sẽ tự động stream, không cần endpoint riêng

    @Multipart
    @POST("api/songs")
    suspend fun uploadSong(
        @Part("title") title: RequestBody,
        @Part("artist") artistId: RequestBody,
        @Part("album") album: RequestBody,
        @Part("genre") genre: RequestBody,
        @Part audio: MultipartBody.Part,
        @Part image: MultipartBody.Part?
    ): SingleSongResponse

    // ===== ARTISTS =====

    @GET("api/artists")
    suspend fun getAllArtists(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ArtistResponse

    @GET("api/artists/{id}")
    suspend fun getArtistById(@Path("id") id: String): SingleArtistResponse

    @GET("api/artists/{id}/songs")
    suspend fun getArtistSongs(@Path("id") artistId: String): SongResponse

    // ===== PLAYLISTS =====

    @GET("api/playlists")
    suspend fun getAllPlaylists(): PlaylistResponse

    @GET("api/playlists/{id}")
    suspend fun getPlaylistById(@Path("id") id: String): SinglePlaylistResponse
}

