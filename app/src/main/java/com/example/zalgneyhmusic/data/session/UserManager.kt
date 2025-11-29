package com.example.zalgneyhmusic.data.session

import com.example.zalgneyhmusic.data.model.domain.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Quản lý phiên làm việc của User hiện tại.
 * Lưu trữ thông tin User sau khi đăng nhập để dùng toàn app.
 */
@Singleton
class UserManager @Inject constructor() {

    // Biến lưu trữ User hiện tại (null nếu chưa đăng nhập)
    var currentUser: User? = null
        private set // Chỉ cho phép set thông qua hàm saveUserSession

    private val _favoriteSongIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteSongIds: StateFlow<Set<String>> = _favoriteSongIds.asStateFlow()

    /**
     * Lưu thông tin user sau khi Login/Sync thành công
     */
    fun saveUserSession(user: User) {
        this.currentUser = user
        // TODO: Sau này có thể lưu thêm vào DataStore để khi mở lại app không bị mất
    }

    // [MỚI] Hàm cập nhật danh sách tim (gọi khi load playlist)
    fun setFavoriteSongIds(ids: List<String>) {
        _favoriteSongIds.value = ids.toSet()
    }

    // [MỚI] Hàm thêm 1 bài vào danh sách tim (gọi khi bấm tim thành công)
    fun addFavoriteSong(songId: String) {
        _favoriteSongIds.update { it + songId }
    }

    // [MỚI] Hàm xóa 1 bài khỏi danh sách tim
    fun removeFavoriteSong(songId: String) {
        _favoriteSongIds.update { it - songId }
    }

    // [MỚI] Kiểm tra nhanh 1 bài hát có được thích không
    fun isSongFavorite(songId: String): Boolean {
        return _favoriteSongIds.value.contains(songId)
    }

    fun clearSession() {
        this.currentUser = null
        _favoriteSongIds.value = emptySet()
    }

    /**
     * Helper lấy nhanh ID của playlist yêu thích
     */
    val favoritePlaylistId: String?
        get() = currentUser?.favoritePlaylistId
}