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

    /**
     * Restores user session when the application is launched.
     * Synchronizes user data from backend and loads favorite songs and followed artists.
     */
    private fun restoreUserSession() {
        viewModelScope.launch {
            // Only restore if session is empty (app restart scenario)
            if (userManager.currentUser == null) {
                val firebaseUser = authRepository.currentUser

                if (firebaseUser != null) {
                    Log.d("APP_INIT", "User already logged in. Restoring session...")

                    // Sync user to get favorites playlist ID
                    val syncResult = authRepository.syncUserToBackEnd()

                    if (syncResult is Resource.Success) {
                        // Save user to UserManager
                        userManager.saveUserSession(syncResult.result)

                        // Load playlists and followed artists (repository will auto-extract favorites to update UserManager)
                        val playlistResult = musicRepository.getMyPlaylists()
                        val artistResult = musicRepository.getFollowedArtists()

                        if (playlistResult is Resource.Success && artistResult is Resource.Success) {
                            Log.d(
                                "APP_INIT",
                                "Session restored successfully! Loaded ${userManager.favoriteSongIds.value.size} favorite songs and ${userManager.followedArtistIds.value.size} followed artists."
                            )
                        }
                    } else {
                        Log.e("APP_INIT", "User sync error: $syncResult")
                    }
                } else {
                    Log.d("APP_INIT", "No user logged in.")
                }
            }
        }
    }
}