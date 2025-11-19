package com.example.zalgneyhmusic.ui.fragment.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.databinding.FragmentSearchBinding
import com.example.zalgneyhmusic.ui.adapter.search.RecentSearchesAdapter
import com.example.zalgneyhmusic.ui.adapter.search.SearchAlbumsAdapter
import com.example.zalgneyhmusic.ui.adapter.search.SearchArtistsAdapter
import com.example.zalgneyhmusic.ui.adapter.search.SearchSongsAdapter
import com.example.zalgneyhmusic.ui.viewmodel.fragment.PlayerViewModel
import com.example.zalgneyhmusic.ui.viewmodel.fragment.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Search Fragment - Search for Songs, Artists, Albums
 * Features: Real-time search with debounce, recent searches, filter by category
 */
@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private val searchViewModel: SearchViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()

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
                // Play the song
                searchViewModel.searchResults.value.songs.let { songs ->
                    playerViewModel.setPlaylist(songs, songs.indexOf(song))
                }
                Toast.makeText(
                    context,
                    getString(R.string.toast_playing, song.title),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        binding.rvSongs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songsAdapter
        }

        // Artists Adapter
        artistsAdapter = SearchArtistsAdapter(
            onArtistClick = { artist ->
                Toast.makeText(
                    context,
                    getString(R.string.toast_artist, artist.name),
                    Toast.LENGTH_SHORT
                ).show()
                // TODO: Navigate to artist detail
            }
        )
        binding.rvArtists.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = artistsAdapter
        }

        // Albums Adapter
        albumsAdapter = SearchAlbumsAdapter(
            onAlbumClick = { album ->
                Toast.makeText(
                    context,
                    getString(R.string.toast_album, album.title),
                    Toast.LENGTH_SHORT
                ).show()
                // TODO: Navigate to album detail
            }
        )
        binding.rvAlbums.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = albumsAdapter
        }

        // Recent Searches Adapter
        recentSearchesAdapter = RecentSearchesAdapter(
            onSearchClick = { query ->
                binding.etSearch.setText(query)
                binding.etSearch.setSelection(query.length)
            },
            onRemoveClick = { query ->
                searchViewModel.removeFromRecentSearches(query)
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
                searchViewModel.recentSearches.collect { recentSearches ->
                    recentSearchesAdapter.submitList(recentSearches)
                    binding.recentSearchesContainer.isVisible =
                        recentSearches.isNotEmpty() && searchViewModel.searchQuery.value.isBlank()
                }
            }
        }
    }

    private fun updateUIState(query: String) {
        when {
            query.isBlank() -> {
                // Show empty state or recent searches
                binding.emptyState.isVisible = searchViewModel.recentSearches.value.isEmpty()
                binding.recentSearchesContainer.isVisible =
                    searchViewModel.recentSearches.value.isNotEmpty()
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