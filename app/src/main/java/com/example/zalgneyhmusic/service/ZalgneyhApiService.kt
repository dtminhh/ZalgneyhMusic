package com.example.zalgneyhmusic.service

import com.example.zalgneyhmusic.data.model.api.AlbumDTO
import com.example.zalgneyhmusic.data.model.api.ApiResponse
import com.example.zalgneyhmusic.data.model.api.ArtistDTO
import com.example.zalgneyhmusic.data.model.api.PlaylistDTO
import com.example.zalgneyhmusic.data.model.api.SongDTO
import com.example.zalgneyhmusic.data.model.api.UserDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API Service for Zalgneyh Music Backend
 * Base URL: https://zalgneyh-backend-production.up.railway.app/api/
 *
 * All endpoints return ApiResponse<T> wrapper: {success, data, message}
 * Based on: API_IMPLEMENTATION_GUIDE.md
 */
interface ZalgneyhApiService {

    /**
     * Syncs the authenticated user with the backend.
     * Requires a valid Bearer token in the Authorization header.
     */
    @POST("auth/sync")
    suspend fun syncUser(@Header("Authorization") token: String): Response<ApiResponse<UserDTO>>

    // ==================== SONGS API ====================

    /**
     * GET /api/songs - Get all songs with pagination
     * Response: { success, data: [SongDTO], pagination }
     */
    @GET("songs")
    suspend fun getAllSongs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<SongDTO>>>

    /**
     * GET /api/songs/trending - Get trending songs (already sorted by popularity)
     */
    @GET("songs/trending")
    suspend fun getTrendingSongs(
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<SongDTO>>>

    /**
     * GET /api/songs/new - Get new songs
     */
    @GET("songs/new")
    suspend fun getNewSongs(
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<SongDTO>>>

    /**
     * GET /api/songs/:id - Get song by ID
     */
    @GET("songs/{id}")
    suspend fun getSongById(
        @Path("id") id: String
    ): Response<ApiResponse<SongDTO>>

    // ==================== ARTISTS API ====================

    /**
     * GET /api/artists - Get all artists with pagination
     */
    @GET("artists")
    suspend fun getArtists(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<ArtistDTO>>>

    /**
     * GET /api/artists/:id - Get artist by ID with albums and songs
     */
    @GET("artists/{id}")
    suspend fun getArtistById(
        @Path("id") id: String
    ): Response<ApiResponse<ArtistDTO>>

    /**
     * GET /api/artists/:id/songs - Get all songs by artist
     */
    @Suppress("unused")
    @GET("artists/{id}/songs")
    suspend fun getArtistSongs(
        @Path("id") artistId: String
    ): Response<ApiResponse<List<SongDTO>>>

    // ==================== ALBUMS API ====================

    /**
     * GET /api/albums - Get all albums
     */
    @GET("albums")
    suspend fun getAlbums(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<AlbumDTO>>>

    /**
     * GET /api/albums/:id - Get album by ID with songs
     */
    @GET("albums/{id}")
    suspend fun getAlbumById(
        @Path("id") id: String
    ): Response<ApiResponse<AlbumDTO>>

    /**
     * GET /api/albums/artist/:artistId - Get albums by artist
     */
    @Suppress("unused")
    @GET("albums/artist/{artistId}")
    suspend fun getAlbumsByArtist(
        @Path("artistId") artistId: String
    ): Response<ApiResponse<List<AlbumDTO>>>

    // ==================== PLAYLISTS API ====================

    /**
     * GET /api/playlists - Get all playlists
     */
    @Suppress("unused")
    @GET("playlists")
    suspend fun getPlaylists(): Response<ApiResponse<List<PlaylistDTO>>>

    /**
     * GET /api/playlists/:id - Get playlist by ID
     */
    @Suppress("unused")
    @GET("playlists/{id}")
    suspend fun getPlaylistById(
        @Path("id") id: String
    ): Response<ApiResponse<PlaylistDTO>>

    // ==================== HEALTH CHECK ====================

    /**
     * GET /health - Check API health status
     */
    @Suppress("unused")
    @GET("health")
    suspend fun healthCheck(): Response<Map<String, Any>>
}
