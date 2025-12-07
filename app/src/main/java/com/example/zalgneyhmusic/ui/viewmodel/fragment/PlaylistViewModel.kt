package com.example.zalgneyhmusic.ui.viewmodel.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.model.Resource
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
    private val musicRepository: MusicRepository,
    private val userManager: UserManager,
    private val authRepository: AuthRepository
) : BaseViewModel() {

    // User playlists state
    private val _userPlaylists = MutableLiveData<Resource<List<Playlist>>>()
    val userPlaylists: LiveData<Resource<List<Playlist>>> = _userPlaylists

    // Playlist creation state (null means idle)
    private val _createPlaylistState = MutableLiveData<Resource<Playlist>?>(null)
    val createPlaylistState: LiveData<Resource<Playlist>?> = _createPlaylistState

    // Generic action state (edit/delete/update)
    private val _actionState = MutableLiveData<Resource<String>>()

    init {
        loadMyPlaylists()
    }

    /** Loads current user's playlists from backend. */
    fun loadMyPlaylists() {
        viewModelScope.launch {
            _userPlaylists.value = Resource.Loading

            var currentUser = userManager.currentUserValue
            if (currentUser == null) {
                val firebaseUser = authRepository.currentUser
                if (firebaseUser != null) {
                    val syncResult = authRepository.syncUserToBackEnd()
                    if (syncResult is Resource.Success) {
                        currentUser = syncResult.result
                        userManager.saveUserSession(currentUser)
                    }
                }
            }

            if (currentUser != null) {
                _userPlaylists.value = musicRepository.getMyPlaylists()
            } else {
                _userPlaylists.value =
                    Resource.Failure(Exception("error_sign_in_required"))
            }
        }
    }

    /** Creates a new playlist for the current user. */
    fun createPlaylist(name: String, description: String? = null, imageFile: java.io.File? = null) {
        if (userManager.currentUserValue == null) return

        viewModelScope.launch {
            _createPlaylistState.value = Resource.Loading
            val result = musicRepository.createPlaylist(name, description, imageFile)

            _createPlaylistState.value = result
            if (result is Resource.Success) {
                loadMyPlaylists() // refresh after creation
            }
        }
    }

    @Suppress("unused")
    fun isFavoritePlaylist(playlistId: String): Boolean {
        return userManager.favoritePlaylistId == playlistId
    }

    @Suppress("unused")
    fun deletePlaylist(id: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val result = musicRepository.deletePlaylist(id)
            _actionState.value = if (result is Resource.Success) {
                loadMyPlaylists()
                Resource.Success("playlist_deleted")
            } else if (result is Resource.Failure) {
                Resource.Failure(result.exception)
            } else Resource.Failure(Exception("unknown_result"))
        }
    }

    fun updatePlaylist(id: String, newName: String, newImage: java.io.File?) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val result = musicRepository.updatePlaylist(id, newName, newImage)
            _actionState.value = if (result is Resource.Success) {
                loadMyPlaylists()
                Resource.Success("playlist_updated_success")
            } else if (result is Resource.Failure) {
                Resource.Failure(result.exception)
            } else Resource.Failure(Exception("unknown_result"))
        }
    }

    fun resetCreateState() {
        _createPlaylistState.value = null
    }
}