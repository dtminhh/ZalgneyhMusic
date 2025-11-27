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
 * Quản lý phiên làm việc của User hiện tại.
 * Lưu trữ thông tin User sau khi đăng nhập để dùng toàn app.
 */
@Singleton
class UserManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Biến lưu trữ User hiện tại (null nếu chưa đăng nhập)
    var currentUser: User? = null
        private set // Chỉ cho phép set thông qua hàm saveUserSession


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
     * Lưu thông tin user sau khi Login/Sync thành công
     */
    fun saveUserSession(user: User) {
        this.currentUser = user
        // TODO: Sau này có thể lưu thêm vào DataStore để khi mở lại app không bị mất
    }

    // [MỚI] Hàm cập nhật danh sách tim (gọi khi load playlist)
    fun setFavoriteSongIds(ids: List<String>) {
        val idSet = ids.toSet()
        _favoriteSongIds.value = idSet
        saveFavoritesToCache(idSet)
    }

    // [MỚI] Hàm thêm 1 bài vào danh sách tim (gọi khi bấm tim thành công)
    fun addFavoriteSong(songId: String) {
        _favoriteSongIds.update { current ->
            val newSet = current + songId
            saveFavoritesToCache(newSet) // Lưu ngay
            newSet
        }
    }

    // [MỚI] Hàm xóa 1 bài khỏi danh sách tim
    fun removeFavoriteSong(songId: String) {
        _favoriteSongIds.update { current ->
            val newSet = current - songId
            saveFavoritesToCache(newSet) // Lưu ngay
            newSet
        }
    }

    // [MỚI] Kiểm tra nhanh 1 bài hát có được thích không
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

        private const val KEY_FOLLOWED_ARTIST_IDS = "followed_artist_ids" // Key mới cho SharedPreferences
    }

}