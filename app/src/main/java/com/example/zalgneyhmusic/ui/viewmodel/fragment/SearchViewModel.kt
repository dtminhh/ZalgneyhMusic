package com.example.zalgneyhmusic.ui.viewmodel.fragment

import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.local.MusicDatabase
import com.example.zalgneyhmusic.data.local.entity.SearchHistoryEntity
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import com.example.zalgneyhmusic.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Search Functionality
 * Supports real-time search for Songs, Artists, Albums with debounce
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val database: MusicDatabase,
    private val musicRepository: MusicRepository
) : BaseViewModel() {

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 500L
        private const val MIN_SEARCH_LENGTH = 2
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

    val searchHistory = database.searchHistoryDao().getHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
            // Gọi API tìm kiếm tổng hợp (Cần implement trong Repository)
            // API này sẽ trả về cả Songs, Artists, Albums dựa trên 'searchString' ở Backend
            musicRepository.searchEverything(query).collect { resource ->
                _isSearching.value = false

                when(resource) {
                    is Resource.Success -> {
                        _searchResults.value = resource.result
                    }
                    is Resource.Failure -> {
                        // Handle error (show toast/log)
                        _searchResults.value = SearchResults() // Clear on error
                    }
                    else -> {}
                }
            }
        }
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

    fun addSongToHistory(song: Song) = viewModelScope.launch(Dispatchers.IO) {
        val entity = SearchHistoryEntity(
            id = song.id,
            title = song.title,
            subtitle = song.artist.name,
            imageUrl = song.imageUrl,
            type = "SONG"
        )
        database.searchHistoryDao().insert(entity)
    }

    // Gọi hàm này khi User CLICK vào một Nghệ sĩ
    fun addArtistToHistory(artist: Artist) = viewModelScope.launch(Dispatchers.IO) {
        val entity = SearchHistoryEntity(
            id = artist.id,
            title = artist.name,
            subtitle = "Nghệ sĩ", // Hoặc số followers
            imageUrl = artist.imageUrl,
            type = "ARTIST"
        )
        database.searchHistoryDao().insert(entity)
    }

    // Gọi hàm này khi User CLICK vào một Album
    fun addAlbumToHistory(album: Album) = viewModelScope.launch(Dispatchers.IO) {
        val entity = SearchHistoryEntity(
            id = album.id,
            title = album.title,
            subtitle = album.artist.name,
            imageUrl = album.image,
            type = "ALBUM"
        )
        database.searchHistoryDao().insert(entity)
    }

    /**
     * Clear recent searches
     */
    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    fun removeFromHistory(item: SearchHistoryEntity) = viewModelScope.launch(Dispatchers.IO) {
        database.searchHistoryDao().delete(item.id)
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