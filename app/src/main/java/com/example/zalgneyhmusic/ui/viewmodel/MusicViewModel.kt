package com.example.zalgneyhmusic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.Song
import com.example.zalgneyhmusic.data.model.Artist
import com.example.zalgneyhmusic.data.model.Playlist
import com.example.zalgneyhmusic.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {
    
    // Songs state
    private val _songs = MutableStateFlow<Resource<List<Song>>>(Resource.Loading)
    val songs: StateFlow<Resource<List<Song>>> = _songs.asStateFlow()
    
    // Trending songs state
    private val _trendingSongs = MutableStateFlow<Resource<List<Song>>>(Resource.Loading)
    val trendingSongs: StateFlow<Resource<List<Song>>> = _trendingSongs.asStateFlow()
    
    // New songs state
    private val _newSongs = MutableStateFlow<Resource<List<Song>>>(Resource.Loading)
    val newSongs: StateFlow<Resource<List<Song>>> = _newSongs.asStateFlow()
    
    // Search results state
    private val _searchResults = MutableStateFlow<Resource<List<Song>>>(Resource.Loading)
    val searchResults: StateFlow<Resource<List<Song>>> = _searchResults.asStateFlow()
    
    // Artists state
    private val _artists = MutableStateFlow<Resource<List<Artist>>>(Resource.Loading)
    val artists: StateFlow<Resource<List<Artist>>> = _artists.asStateFlow()
    
    // Playlists state
    private val _playlists = MutableStateFlow<Resource<List<Playlist>>>(Resource.Loading)
    val playlists: StateFlow<Resource<List<Playlist>>> = _playlists.asStateFlow()
    
    // Current song state
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        loadTrendingSongs()
        loadNewSongs()
        loadAllArtists()
    }
    
    fun loadAllSongs(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            repository.getAllSongs(page, limit).collect { resource ->
                _songs.value = resource
            }
        }
    }

    fun loadTrendingSongs(limit: Int = 20) {
        viewModelScope.launch {
            repository.getTrendingSongs(limit).collect { resource ->
                _trendingSongs.value = resource
            }
        }
    }

    fun loadNewSongs(limit: Int = 20) {
        viewModelScope.launch {
            repository.getNewSongs(limit).collect { resource ->
                _newSongs.value = resource
            }
        }
    }

    fun searchSongs(query: String) {
        if (query.isBlank()) {
            _searchResults.value = Resource.Success(emptyList())
            return
        }

        viewModelScope.launch {
            repository.searchSongs(query).collect { resource ->
                _searchResults.value = resource
            }
        }
    }

    fun loadSongsByGenre(genre: String, limit: Int = 20) {
        viewModelScope.launch {
            repository.getSongsByGenre(genre, limit).collect { resource ->
                _songs.value = resource
            }
        }
    }

    fun loadAllArtists(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            repository.getAllArtists(page, limit).collect { resource ->
                _artists.value = resource
            }
        }
    }

    fun loadArtistSongs(artistId: String) {
        viewModelScope.launch {
            repository.getArtistSongs(artistId).collect { resource ->
                _songs.value = resource
            }
        }
    }

    fun loadAllPlaylists() {
        viewModelScope.launch {
            repository.getAllPlaylists().collect { resource ->
                _playlists.value = resource
            }
        }
    }

    fun setCurrentSong(song: Song) {
        _currentSong.value = song
    }

    fun clearSearchResults() {
        _searchResults.value = Resource.Success(emptyList())
    }
}
