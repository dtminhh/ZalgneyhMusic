package com.example.zalgneyhmusic.ui.viewmodel.fragment

import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import com.example.zalgneyhmusic.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Search Functionality
 * Supports real-time search for Songs, Artists, Albums with debounce
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : BaseViewModel() {

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 500L
        private const val MIN_SEARCH_LENGTH = 2
        private const val MAX_RECENT_SEARCHES = 10
    }

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Search results
    private val _searchResults = MutableStateFlow<SearchResults>(SearchResults())
    val searchResults: StateFlow<SearchResults> = _searchResults.asStateFlow()

    // Loading state
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Recent searches
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    init {
        observeSearchQuery()
    }

    /**
     * Observe search query with 500ms debounce
     */
    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(SEARCH_DEBOUNCE_MS) // Wait after user stops typing
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank() && query.length >= MIN_SEARCH_LENGTH) {
                        performSearch(query)
                    } else {
                        clearSearchResults()
                    }
                }
        }
    }

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Perform search
     */
    private fun performSearch(query: String) {
        _isSearching.value = true

        viewModelScope.launch {
            // Search songs
            launch {
                musicRepository.searchSongs(query).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _searchResults.value = _searchResults.value.copy(
                                songs = resource.result
                            )
                        }

                        is Resource.Failure -> {
                            // Handle error
                        }

                        is Resource.Loading -> {}
                    }
                }
            }

            // Search artists (filter by name)
            launch {
                musicRepository.getAllArtists().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val filteredArtists = resource.result.filter { artist ->
                                artist.name.contains(query, ignoreCase = true)
                            }
                            _searchResults.value = _searchResults.value.copy(
                                artists = filteredArtists
                            )
                        }

                        is Resource.Failure -> {
                            // Handle error
                        }

                        is Resource.Loading -> {}
                    }
                }
            }

            // Search albums (filter by title)
            launch {
                musicRepository.getAllAlbums().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val filteredAlbums = resource.result.filter { album ->
                                album.title.contains(query, ignoreCase = true)
                            }
                            _searchResults.value = _searchResults.value.copy(
                                albums = filteredAlbums
                            )
                            _isSearching.value = false
                        }

                        is Resource.Failure -> {
                            _isSearching.value = false
                        }

                        is Resource.Loading -> {}
                    }
                }
            }
        }

        // Add to recent searches
        addToRecentSearches(query)
    }

    /**
     * Clear search results
     */
    fun clearSearchResults() {
        _searchResults.value = SearchResults()
        _isSearching.value = false
    }

    /**
     * Clear search query
     */
    fun clearSearch() {
        _searchQuery.value = ""
        clearSearchResults()
    }

    /**
     * Add to recent searches
     */
    private fun addToRecentSearches(query: String) {
        val currentSearches = _recentSearches.value.toMutableList()
        currentSearches.remove(query) // Remove if already exists
        currentSearches.add(0, query) // Add to beginning
        _recentSearches.value = currentSearches.take(MAX_RECENT_SEARCHES)
    }

    /**
     * Clear recent searches
     */
    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    /**
     * Remove from recent searches
     */
    fun removeFromRecentSearches(query: String) {
        _recentSearches.value = _recentSearches.value.filter { it != query }
    }
}

/**
 * Data class cho search results
 */
data class SearchResults(
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList()
) {
    fun isEmpty(): Boolean = songs.isEmpty() && artists.isEmpty() && albums.isEmpty()
    fun isNotEmpty(): Boolean = !isEmpty()
}