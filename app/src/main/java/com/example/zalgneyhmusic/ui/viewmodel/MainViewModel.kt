package com.example.zalgneyhmusic.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.repository.auth.AuthRepository
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import com.example.zalgneyhmusic.data.session.UserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val musicRepository: MusicRepository,
    private val userManager: UserManager
) : ViewModel() {

    init {
        restoreUserSession()
    }

    private fun restoreUserSession() {
        viewModelScope.launch {
            // Chỉ khôi phục nếu Session đang rỗng (trường hợp mở lại app)
            if (userManager.currentUser == null) {
                val firebaseUser = authRepository.currentUser

                if (firebaseUser != null) {
                    Log.d("APP_INIT", "Phát hiện User đã đăng nhập. Đang khôi phục Session...")

                    // 1. Sync User để lấy ID Playlist Favorites
                    val syncResult = authRepository.syncUserToBackEnd()

                    if (syncResult is Resource.Success) {
                        // Lưu User vào UserManager
                        userManager.saveUserSession(syncResult.result)

                        // 2. Tải danh sách Playlist (Repo sẽ tự động trích xuất Favorites để cập nhật UserManager)
                        val playlistResult = musicRepository.getMyPlaylists()
                        val artistResult = musicRepository.getFollowedArtists()

                        if (playlistResult is Resource.Success && artistResult is Resource.Success) {
                            Log.d(
                                "APP_INIT",
                                "Khôi phục thành công! Đã nạp ${userManager.favoriteSongIds.value.size} bài hát yêu thích và ${userManager.followedArtistIds.value.size} nghệ sĩ theo dõi."
                            )
                        }
                    } else {
                        Log.e("APP_INIT", "Lỗi Sync User: $syncResult")
                    }
                } else {
                    Log.d("APP_INIT", "Chưa có User đăng nhập.")
                }
            }
        }
    }
}