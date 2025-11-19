package com.example.zalgneyhmusic.ui.viewmodel.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Playlist
import com.example.zalgneyhmusic.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for Playlists screen
 */
@HiltViewModel
class PlaylistViewModel @Inject constructor(
    // TODO: Inject PlaylistRepository when available
) : BaseViewModel() {

    companion object {
        private const val DEMO_DATA_DELAY_MS = 1000L
    }

    private val _playlists = MutableLiveData<Resource<List<Playlist>>>()
    val playlists: LiveData<Resource<List<Playlist>>> = _playlists

    private val _userPlaylists = MutableLiveData<Resource<List<Playlist>>>()
    val userPlaylists: LiveData<Resource<List<Playlist>>> = _userPlaylists

    init {
        loadPlaylists()
        loadUserPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            _playlists.value = Resource.Loading

            try {
                delay(DEMO_DATA_DELAY_MS)
                val demoPlaylists = getDemoPlaylists()
                _playlists.value = Resource.Success(demoPlaylists)
            } catch (e: Exception) {
                _playlists.value = Resource.Failure(e)
            }
        }
    }

    fun loadUserPlaylists() {
        viewModelScope.launch {
            _userPlaylists.value = Resource.Loading

            try {
                delay(DEMO_DATA_DELAY_MS)
                val userPlaylists = getDemoPlaylists().filter { it.createdBy != "Admin" }
                _userPlaylists.value = Resource.Success(userPlaylists)
            } catch (e: Exception) {
                _userPlaylists.value = Resource.Failure(e)
            }
        }
    }

    private fun getDemoPlaylists(): List<Playlist> {
        return listOf(
            Playlist(
                id = "1",
                name = "Today's Top Hits",
                description = "Ed Sheeran is on top of the Hottest 50!",
                imageUrl = "https://i.scdn.co/image/ab67706f00000003c6ffc45abae6a4f1a0b27ec2",
                songs = listOf("s1", "s2", "s3", "s4", "s5"),
                isPublic = true,
                createdBy = "Spotify"
            ),
            Playlist(
                id = "2",
                name = "RapCaviar",
                description = "New music from Drake, Travis Scott, and more.",
                imageUrl = "https://i.scdn.co/image/ab67706f00000003fc89c91fa34f4f09a68d5c5f",
                songs = listOf("s6", "s7", "s8"),
                isPublic = true,
                createdBy = "Spotify"
            ),
            Playlist(
                id = "3",
                name = "All Out 2000s",
                description = "The biggest songs of the 2000s.",
                imageUrl = "https://i.scdn.co/image/ab67706f00000003e4eadd417a05b2546d866934",
                songs = listOf("s9", "s10", "s11", "s12"),
                isPublic = true,
                createdBy = "Spotify"
            ),
            Playlist(
                id = "4",
                name = "Rock Classics",
                description = "Rock legends & epic songs.",
                imageUrl = "https://i.scdn.co/image/ab67706f000000039249369e44ae14515dd7fc37",
                songs = listOf("s13", "s14", "s15"),
                isPublic = true,
                createdBy = "Spotify"
            ),
            Playlist(
                id = "5",
                name = "My Favorites",
                description = "My personal collection",
                imageUrl = "https://i.scdn.co/image/ab67706c0000da841841a3de0e5846647d7e8b24",
                songs = listOf("s1", "s5", "s10"),
                isPublic = false,
                createdBy = "Me"
            )
        )
    }
}

