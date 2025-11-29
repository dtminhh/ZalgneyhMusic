package com.example.zalgneyhmusic.ui.viewmodel.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Playlist
import com.example.zalgneyhmusic.data.repository.auth.AuthRepository
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import com.example.zalgneyhmusic.data.session.UserManager
import com.example.zalgneyhmusic.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val musicRepository: MusicRepository, // [MỚI] Inject Repository
    private val userManager: UserManager ,
    private val authRepository: AuthRepository// [MỚI] Để check User
) : BaseViewModel() {

    // LiveData chứa danh sách Playlist của User
    private val _userPlaylists = MutableLiveData<Resource<List<Playlist>>>()
    val userPlaylists: LiveData<Resource<List<Playlist>>> = _userPlaylists

    // LiveData trạng thái tạo Playlist (để UI biết khi nào tạo xong)
    private val _createPlaylistState = MutableLiveData<Resource<Playlist>?>(null)
    val createPlaylistState: LiveData<Resource<Playlist>?> = _createPlaylistState

    init {
        loadMyPlaylists()
    }

    /**
     * Tải danh sách Playlist cá nhân từ Server
     */
    fun loadMyPlaylists() {
        viewModelScope.launch {
            _userPlaylists.value = Resource.Loading

            // BƯỚC 1: Kiểm tra User trong RAM (UserManager)
            var currentUser = userManager.currentUser

            // BƯỚC 2: Nếu RAM rỗng, kiểm tra xem Firebase có đang đăng nhập không (Trường hợp mở lại app)
            if (currentUser == null) {
                val firebaseUser = authRepository.currentUser
                if (firebaseUser != null) {
                    // Có Firebase User -> Gọi Sync để lấy lại thông tin User từ Backend
                    val syncResult = authRepository.syncUserToBackEnd()

                    if (syncResult is Resource.Success) {
                        // Sync thành công -> Lưu lại vào RAM
                        currentUser = syncResult.result
                        userManager.saveUserSession(currentUser)
                    }
                }
            }

            // BƯỚC 3: Xử lý dựa trên kết quả cuối cùng
            if (currentUser != null) {
                // Đã có User (từ RAM hoặc mới Sync xong) -> Lấy Playlist
                val result = musicRepository.getMyPlaylists()
                _userPlaylists.value = result
            } else {
                // Vẫn không có User -> Bắt buộc đăng nhập
                _userPlaylists.value = Resource.Failure(Exception("Vui lòng đăng nhập để xem Playlist"))
            }
        }
    }

    /**
     * Tạo Playlist mới
     */
    fun createPlaylist(name: String) {
        if (userManager.currentUser == null) return

        viewModelScope.launch {
            _createPlaylistState.value = Resource.Loading
            val result = musicRepository.createPlaylist(name)

            if (result is Resource.Success) {
                // Tạo thành công -> Reload lại danh sách ngay lập tức
                loadMyPlaylists()
                _createPlaylistState.value = result
            } else {
                _createPlaylistState.value = result
            }
        }
    }

    fun resetCreateState() {
        _createPlaylistState.value = null
    }
}