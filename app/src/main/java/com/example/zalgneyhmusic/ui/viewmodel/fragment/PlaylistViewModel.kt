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
    private val musicRepository: MusicRepository,
    private val userManager: UserManager,
    private val authRepository: AuthRepository
) : BaseViewModel() {

    // Holds the current user's playlists
    private val _userPlaylists = MutableLiveData<Resource<List<Playlist>>>()
    val userPlaylists: LiveData<Resource<List<Playlist>>> = _userPlaylists

    // Tracks playlist creation state so UI knows when creation is finished
    private val _createPlaylistState = MutableLiveData<Resource<Playlist>?>(null)
    val createPlaylistState: LiveData<Resource<Playlist>?> = _createPlaylistState

    private val _actionState = MutableLiveData<Resource<String>>()

    init {
        loadMyPlaylists()
    }

    /**
     * Loads the user's personal playlists from the backend.
     */
    fun loadMyPlaylists() {
        viewModelScope.launch {
            _userPlaylists.value = Resource.Loading

            // Step 1: Try to read the user from in-memory session (UserManager)
            var currentUser = userManager.currentUser

            // Step 2: If missing, check if Firebase has an active session (app reopened case)
            if (currentUser == null) {
                val firebaseUser = authRepository.currentUser
                if (firebaseUser != null) {
                    // Firebase user exists -> sync account data from backend
                    val syncResult = authRepository.syncUserToBackEnd()

                    if (syncResult is Resource.Success) {
                        // Persist synced user in memory for subsequent calls
                        currentUser = syncResult.result
                        userManager.saveUserSession(currentUser)
                    }
                }
            }

            // Step 3: Continue based on the final resolved user
            if (currentUser != null) {
                // User is available (from RAM or freshly synced) -> load playlists
                val result = musicRepository.getMyPlaylists()
                _userPlaylists.value = result
            } else {
                // Still no user -> force login before accessing playlists
                _userPlaylists.value =
                    Resource.Failure(Exception("Please sign in to view playlists"))
            }
        }
    }

    /**
     * Creates a new playlist for the current user.
     */
    fun createPlaylist(name: String, description: String? = null, imageFile: java.io.File? = null) {
        if (userManager.currentUser == null) return

        viewModelScope.launch {
            _createPlaylistState.value = Resource.Loading
            val result = musicRepository.createPlaylist(name, description, imageFile)

            if (result is Resource.Success) {
                // Refresh playlists immediately after successful creation
                loadMyPlaylists()
                _createPlaylistState.value = result
            } else {
                _createPlaylistState.value = result
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
            if (result is Resource.Success) {
                _actionState.value = Resource.Success("Playlist deleted")
                // Reload list to reflect deletion in UI
                loadMyPlaylists()
            } else if (result is Resource.Failure) {
                _actionState.value = Resource.Failure(result.exception)
            }
        }
    }

    fun updatePlaylist(id: String, newName: String, newImage: java.io.File?) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val result = musicRepository.updatePlaylist(id, newName, newImage)
            if (result is Resource.Success) {
                _actionState.value = Resource.Success("Playlist updated successfully")
                loadMyPlaylists()
            } else if (result is Resource.Failure) {
                _actionState.value = Resource.Failure(result.exception)
            }
        }
    }

    fun resetCreateState() {
        _createPlaylistState.value = null
    }
}