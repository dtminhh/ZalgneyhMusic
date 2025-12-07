package com.example.zalgneyhmusic.ui.viewmodel.fragment

import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home Screen
 * Manages data and logic for home sections:
 * - Featured Songs (top 5 trending)
 * - Top Artists (top 10)
 * - Featured Albums (recent 10)
 * - Recently Heard (last 10 played)
 * - Suggestions (personalized recommendations)
 *
 * Uses StateFlow for reactive data streams with efficient updates
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : BaseViewModel() {

    // Featured Songs section (top 5 trending songs)
    private val _featuredSongs = MutableStateFlow<Resource<List<Song>>>(Resource.Loading)
    val featuredSongs: StateFlow<Resource<List<Song>>> = _featuredSongs.asStateFlow()

    // Top Artists section (top 10 artists)
    private val _topArtists = MutableStateFlow<Resource<List<Artist>>>(Resource.Loading)
    val topArtists: StateFlow<Resource<List<Artist>>> = _topArtists.asStateFlow()

    // Featured Albums section (recent 10 albums)
    private val _featuredAlbums = MutableStateFlow<Resource<List<Album>>>(Resource.Loading)
    val featuredAlbums: StateFlow<Resource<List<Album>>> = _featuredAlbums.asStateFlow()

    // Recently Heard section (last 10 played songs)
    private val _recentlyHeard = MutableStateFlow<Resource<List<Song>>>(Resource.Loading)
    val recentlyHeard: StateFlow<Resource<List<Song>>> = _recentlyHeard.asStateFlow()

    // Suggestions section (personalized recommendations)
    private val _suggestions = MutableStateFlow<Resource<List<Song>>>(Resource.Loading)
    val suggestions: StateFlow<Resource<List<Song>>> = _suggestions.asStateFlow()

    init {
        loadHomeData()
    }

    /**
     * Loads all data for Home Screen sections
     * Triggers parallel loading of all sections
     */
    fun loadHomeData() {
        loadFeaturedSongs()
        loadTopArtists()
        loadFeaturedAlbums()
        loadRecentlyHeard()
        loadSuggestions()
    }

    /** Loads top 5 featured songs from repository */
    private fun loadFeaturedSongs() {
        viewModelScope.launch {
            musicRepository.getTopSongs().collect { resource ->
                _featuredSongs.value = resource
            }
        }
    }

    /** Loads top 10 artists from repository */
    private fun loadTopArtists() {
        viewModelScope.launch {
            musicRepository.getTopArtists(LIMIT_LOAD_DATA_VALUE).collect { resource ->
                _topArtists.value = resource
            }
        }
    }

    /** Loads 10 most recent albums from repository */
    private fun loadFeaturedAlbums() {
        viewModelScope.launch {
            musicRepository.getRecentAlbums(LIMIT_LOAD_DATA_VALUE).collect { resource ->
                _featuredAlbums.value = resource
            }
        }
    }

    /** Loads 10 recently played songs from repository */
    private fun loadRecentlyHeard() {
        viewModelScope.launch {
            musicRepository.getListeningHistory().collect { songs ->
                val previewList = songs.take(LIMIT_LOAD_DATA_VALUE)
                _recentlyHeard.value = Resource.Success(previewList)
            }
        }
    }

    /** Loads personalized song suggestions based on user preferences */
    private fun loadSuggestions() {
        viewModelScope.launch {
            // Call the new lightweight ML-based suggestions method
            musicRepository.getPersonalizedSuggestions().collect { resource ->
                _suggestions.value = resource
            }
        }
    }

    /**
     * Refreshes all home screen data
     * Used for pull-to-refresh functionality
     */
    @Suppress("unused") // Will be used when implementing pull-to-refresh
    fun refresh() {
        loadHomeData()
    }

    companion object {
        const val LIMIT_LOAD_DATA_VALUE = 10
    }
}
