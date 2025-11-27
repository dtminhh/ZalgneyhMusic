package com.example.zalgneyhmusic.ui.fragment.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.databinding.FragmentSearchBinding
import com.example.zalgneyhmusic.ui.adapter.search.RecentSearchesAdapter
import com.example.zalgneyhmusic.ui.adapter.search.SearchAlbumsAdapter
import com.example.zalgneyhmusic.ui.adapter.search.SearchArtistsAdapter
import com.example.zalgneyhmusic.ui.adapter.search.SearchSongsAdapter
import com.example.zalgneyhmusic.ui.extension.openAlbumDetail
import com.example.zalgneyhmusic.ui.extension.openArtistDetail
import com.example.zalgneyhmusic.ui.fragment.BaseFragment
import com.example.zalgneyhmusic.ui.viewmodel.fragment.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Search Fragment - Search for Songs, Artists, Albums
 * Features: Real-time search with debounce, recent searches, filter by category
 */
@AndroidEntryPoint
class SearchFragment : BaseFragment() {

    private lateinit var binding: FragmentSearchBinding
    private val searchViewModel: SearchViewModel by viewModels()
    private lateinit var songsAdapter: SearchSongsAdapter
    private lateinit var artistsAdapter: SearchArtistsAdapter
    private lateinit var albumsAdapter: SearchAlbumsAdapter
    private lateinit var recentSearchesAdapter: RecentSearchesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupSearchBar()
        observeSearchState()
    }

    private fun setupAdapters() {
        // Songs Adapter
        songsAdapter = SearchSongsAdapter(
            onSongClick = { song ->
                searchViewModel.addSongToHistory(song)
                mediaActionHandler.onSongClick(song, songsAdapter.currentList)
            }
        )
        binding.rvSongs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songsAdapter
        }

        // Artists Adapter
        artistsAdapter = SearchArtistsAdapter(
            onArtistClick = { artist ->
                searchViewModel.addArtistToHistory(artist)
                openArtistDetail(artist.id)
            }
        )
        binding.rvArtists.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = artistsAdapter
        }

        // Albums Adapter
        albumsAdapter = SearchAlbumsAdapter(
            onAlbumClick = { album ->
                searchViewModel.addAlbumToHistory(album)
                openAlbumDetail(album.id)
            }
        )
        binding.rvAlbums.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = albumsAdapter
        }

        // Recent Searches Adapter
        recentSearchesAdapter = RecentSearchesAdapter(
            onItemClick = { item ->
                when (item.type) {
                    "QUERY" -> {
                        binding.etSearch.setText(item.title)
                        binding.etSearch.setSelection(item.title.length)
                    }

                    "SONG" -> {
                        binding.etSearch.setText(item.title)
                    }

                    "ARTIST" -> openArtistDetail(item.id)
                    "ALBUM" -> openAlbumDetail(item.id)
                }
            },
            onRemoveClick = { item ->
                searchViewModel.removeFromHistory(item)
            }
        )
        binding.rvRecentSearches.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentSearchesAdapter
        }
    }

    private fun setupSearchBar() {
        // Search text watcher
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                searchViewModel.updateSearchQuery(query)
                binding.btnClearSearch.isVisible = query.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString()
                if (query.isNotBlank()) {

                    // Add the entered keyword to the search history
                    searchViewModel.addKeywordToHistory(query)

                    // Hide the keyboard after submitting the search
                    val imm = requireContext()
                        .getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                            as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
                true
            } else false
        }

        // Clear search button
        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text.clear()
            searchViewModel.clearSearch()
        }

        // Clear recent searches button
        binding.btnClearRecentSearches.setOnClickListener {
            searchViewModel.clearRecentSearches()
        }
    }

    private fun observeSearchState() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe search query
            launch {
                searchViewModel.searchQuery.collect { query ->
                    updateUIState(query)
                }
            }

            // Observe search results
            launch {
                searchViewModel.searchResults.collect { results ->
                    // Update songs
                    songsAdapter.submitList(results.songs)
                    binding.songsSection.isVisible = results.songs.isNotEmpty()

                    // Update artists
                    artistsAdapter.submitList(results.artists)
                    binding.artistsSection.isVisible = results.artists.isNotEmpty()

                    // Update albums
                    albumsAdapter.submitList(results.albums)
                    binding.albumsSection.isVisible = results.albums.isNotEmpty()

                    // Show no results if all empty
                    val hasResults = results.isNotEmpty()
                    val isSearching = searchViewModel.searchQuery.value.isNotBlank()
                    binding.noResultsState.isVisible =
                        !hasResults && isSearching && !searchViewModel.isSearching.value
                }
            }

            // Observe loading state
            launch {
                searchViewModel.isSearching.collect { isSearching ->
                    binding.progressBar.isVisible = isSearching
                }
            }

            // Observe recent searches
            launch {
                searchViewModel.searchHistory.collect { historyList ->
                    recentSearchesAdapter.submitList(historyList)
                    // 2. Check UI conditions
                    val isQueryBlank = searchViewModel.searchQuery.value.isBlank()
                    val hasHistory = historyList.isNotEmpty()

                    // 3. Update UI based on the latest state
                    if (isQueryBlank) {
                        if (hasHistory) {
                            // History exists → Show list, hide empty state
                            binding.recentSearchesContainer.isVisible = true
                            binding.emptyState.isVisible = false
                        } else {
                            // No history → Hide list, show empty state
                            binding.recentSearchesContainer.isVisible = false
                            binding.emptyState.isVisible = true
                        }
                    }
                }
            }
        }
    }

    private fun updateUIState(query: String) {
        when {
            query.isBlank() -> {
                val hasHistory = recentSearchesAdapter.currentList.isNotEmpty()
                // Show empty state or recent searches
                binding.emptyState.isVisible = !hasHistory
                binding.recentSearchesContainer.isVisible = hasHistory
                binding.searchResultsContainer.isVisible = false
            }

            else -> {
                // Show search results
                binding.emptyState.isVisible = false
                binding.recentSearchesContainer.isVisible = false
                binding.searchResultsContainer.isVisible = true
            }
        }
    }
}