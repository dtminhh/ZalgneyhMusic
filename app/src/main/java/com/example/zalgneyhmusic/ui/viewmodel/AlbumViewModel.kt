package com.example.zalgneyhmusic.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Album
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Albums screen
 * Manages album data with LiveData for UI observation
 */
@HiltViewModel
class AlbumViewModel @Inject constructor(
    // TODO: Inject AlbumRepository when available
) : BaseViewModel() {

    // Private mutable LiveData for internal updates
    private val _albums = MutableLiveData<Resource<List<Album>>>()

    // Public immutable LiveData for Fragment observation
    val albums: LiveData<Resource<List<Album>>> = _albums

    private val _featuredAlbums = MutableLiveData<Resource<List<Album>>>()
    val featuredAlbums: LiveData<Resource<List<Album>>> = _featuredAlbums

    init {
        loadAlbums()
        loadFeaturedAlbums()
    }

    /** Loads all albums from repository */
    fun loadAlbums() {
        viewModelScope.launch {
            _albums.value = Resource.Loading

            try {
                // TODO: Call real API when repository is available
                delay(COROUTINE_DELAY) // Simulate network delay
                val demoAlbums = getDemoAlbums()
                _albums.value = Resource.Success(demoAlbums)
            } catch (e: Exception) {
                _albums.value = Resource.Failure(e)
            }
        }
    }

    /** Loads featured albums (top 5) */
    fun loadFeaturedAlbums() {
        viewModelScope.launch {
            _featuredAlbums.value = Resource.Loading
            try {
                delay(COROUTINE_DELAY)
                val demoAlbums = getDemoAlbums().take(DEMO_ALBUM_LIMIT)
                _featuredAlbums.value = Resource.Success(demoAlbums)
            } catch (e: Exception) {
                _featuredAlbums.value = Resource.Failure(e)
            }
        }
    }

    private fun getDemoAlbums(): List<Album> {
        return listOf(
            Album(
                id = "1",
                title = "÷ (Divide)",
                artist = "Ed Sheeran",
                releaseYear = 2017,
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273ba5db46f4b838ef6027e6f96",
                totalTracks = 16
            ),
            Album(
                id = "2",
                title = "After Hours",
                artist = "The Weeknd",
                releaseYear = 2020,
                imageUrl = "https://i.scdn.co/image/ab67616d0000b2738863bc11d2aa12b54f5aeb36",
                totalTracks = 14
            ),
            Album(
                id = "3",
                title = "Folklore",
                artist = "Taylor Swift",
                releaseYear = 2020,
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273295b220e1a66da3cd86c9a45",
                totalTracks = 16
            ),
            Album(
                id = "4",
                title = "Fine Line",
                artist = "Harry Styles",
                releaseYear = 2019,
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273d49810dd1cd1210b1e5e12e5",
                totalTracks = 12
            ),
            Album(
                id = "5",
                title = "Future Nostalgia",
                artist = "Dua Lipa",
                releaseYear = 2020,
                imageUrl = "https://i.scdn.co/image/ab67616d0000b2734677c0e13e2f9678e8b9f7f5",
                totalTracks = 11
            ),
            Album(
                id = "6",
                title = "Happier Than Ever",
                artist = "Billie Eilish",
                releaseYear = 2021,
                imageUrl = "https://i.scdn.co/image/ab67616d0000b2732a038d3bf875d23e4aeaa84e",
                totalTracks = 16
            ),
            Album(
                id = "7",
                title = "Positions",
                artist = "Ariana Grande",
                releaseYear = 2020,
                imageUrl = "https://i.scdn.co/image/ab67616d0000b2735ef878a782c987d38d82b605",
                totalTracks = 14
            ),
            Album(
                id = "8",
                title = "Chromatica",
                artist = "Lady Gaga",
                releaseYear = 2020,
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273bdd9b5d7a8b3e11c9556fe11",
                totalTracks = 16
            )
        )
    }

    companion object {
        const val COROUTINE_DELAY = 1000L
        const val DEMO_ALBUM_LIMIT = 5
    }
}