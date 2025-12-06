package com.example.zalgneyhmusic.data.session

import android.content.Context
import android.util.Log
import com.example.zalgneyhmusic.data.model.domain.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Manages the current user session.
 * Stores user information after login for app-wide use.
 */
@Singleton
class UserManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Current logged-in user (null if not logged in).
     * Can only be set through saveUserSession function.
     */
    var currentUser: User? = null
        private set


    private val _favoriteSongIds = MutableStateFlow<Set<String>>(loadFavoritesFromCache())
    val favoriteSongIds: StateFlow<Set<String>> = _favoriteSongIds.asStateFlow()

    private val _followedArtistIds = MutableStateFlow<Set<String>>(emptySet())
    val followedArtistIds: StateFlow<Set<String>> = _followedArtistIds.asStateFlow()

    fun setFollowedArtistIds(ids: List<String>) {
        _followedArtistIds.value = ids.toSet()
    }

    fun followArtist(artistId: String) {
        _followedArtistIds.update { it + artistId }
    }

    fun unfollowArtist(artistId: String) {
        _followedArtistIds.update { it - artistId }
    }

    fun isArtistFollowed(artistId: String): Boolean {
        return _followedArtistIds.value.contains(artistId)
    }

    /**
     * Helper lấy nhanh ID của playlist yêu thích
     */
    val favoritePlaylistId: String?
        get() = currentUser?.favoritePlaylistId

    private fun loadFavoritesFromCache(): Set<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedIds = prefs.getStringSet(KEY_FAV_IDS, emptySet()) ?: emptySet()
        Log.d("UserManager", "Loaded ${savedIds.size} favorites from cache")
        return savedIds
    }

    private fun saveFavoritesToCache(ids: Set<String>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putStringSet(KEY_FAV_IDS, ids) }
        Log.d("UserManager", "Saved ${ids.size} favorites to cache")
    }

    /**
     * Saves user session information after successful login/sync.
     *
     * @param user User data to save
     */
    fun saveUserSession(user: User) {
        this.currentUser = user
        // TODO: Could persist to DataStore in the future to retain across app restarts
    }

    /**
     * Updates the list of favorite song IDs.
     * Called when playlists are loaded.
     *
     * @param ids List of favorite song IDs
     */
    fun setFavoriteSongIds(ids: List<String>) {
        val idSet = ids.toSet()
        _favoriteSongIds.value = idSet
        saveFavoritesToCache(idSet)
    }

    /**
     * Adds a song to favorites list.
     * Called when user favorites a song successfully.
     *
     * @param songId Song ID to add
     */
    fun addFavoriteSong(songId: String) {
        _favoriteSongIds.update { current ->
            val newSet = current + songId
            saveFavoritesToCache(newSet) // Save immediately
            newSet
        }
    }

    /**
     * Removes a song from favorites list.
     *
     * @param songId Song ID to remove
     */
    fun removeFavoriteSong(songId: String) {
        _favoriteSongIds.update { current ->
            val newSet = current - songId
            saveFavoritesToCache(newSet) // Save immediately
            newSet
        }
    }

    /**
     * Checks if a song is favorited.
     *
     * @param songId Song ID to check
     * @return true if song is favorited, false otherwise
     */
    fun isSongFavorite(songId: String): Boolean {
        return _favoriteSongIds.value.contains(songId)
    }

    fun clearSession() {
        this.currentUser = null
        _favoriteSongIds.value = emptySet()
        _followedArtistIds.value = emptySet()

        // Clear cached data on logout
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            remove(KEY_FAV_IDS)
            remove(KEY_FOLLOWED_ARTIST_IDS)
            apply()
        }
        Log.d("UserManager", "Cleared user session")
    }

    companion object {
        private const val PREF_NAME = "user_session_prefs"
        private const val KEY_FAV_IDS = "favorite_song_ids"
        private const val KEY_FOLLOWED_ARTIST_IDS = "followed_artist_ids" // SharedPreferences key for followed artists
    }

}