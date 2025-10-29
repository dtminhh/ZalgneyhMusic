package com.example.zalgneyhmusic.ui.fragment.mainNav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.databinding.FragmentHomeBinding
import com.example.zalgneyhmusic.ui.adapter.home.HomeParentAdapter
import com.example.zalgneyhmusic.ui.model.HomeSection
import com.example.zalgneyhmusic.ui.model.SectionType
import com.example.zalgneyhmusic.ui.viewmodel.HomeViewModel
import com.example.zalgneyhmusic.ui.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Home fragment displaying main landing page with music recommendations.
 * Sections: Featured Songs (5), Top Artists, Featured Albums, Recently Heard, Suggestions
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by activityViewModels()
    private lateinit var homeAdapter: HomeParentAdapter

    /**
     * Creates and returns the fragment view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called after onCreateView to perform additional view setup.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
    }

    /**
     * Sets up the RecyclerView with the HomeParentAdapter.
     */
    private fun setupRecyclerView() {
        homeAdapter = HomeParentAdapter(
            onSongClick = { song, sectionType ->
                // Get correct section songs based on which section was clicked
                val currentSongs = when (sectionType) {
                    SectionType.FEATURED_SONGS ->
                        (viewModel.featuredSongs.value as? Resource.Success)?.result ?: listOf(song)
                    SectionType.RECENTLY_HEARD ->
                        (viewModel.recentlyHeard.value as? Resource.Success)?.result ?: listOf(song)
                    SectionType.SUGGESTIONS ->
                        (viewModel.suggestions.value as? Resource.Success)?.result ?: listOf(song)
                    else -> listOf(song) // For non-song sections
                }

                // Set playlist and play
                val songIndex = currentSongs.indexOf(song).takeIf { it >= DEFAULT_NULL_INT_VALUE }
                    ?: DEFAULT_NULL_INT_VALUE
                playerViewModel.setPlaylist(currentSongs, songIndex)

                // Navigate to player
                try {
                    findNavController().navigate(R.id.action_mainFragment_to_playerFragment)
                } catch (_: Exception) {
                    // Fallback if navigation fails - exception details not needed
                    Toast.makeText(context, "Playing: ${song.title}", Toast.LENGTH_SHORT).show()
                }
            },
            onArtistClick = { artist ->
                Toast.makeText(context, "Artist: ${artist.name}", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to artist detail
            },
            onAlbumClick = { album ->
                Toast.makeText(context, "Album: ${album.title}", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to album detail
            }
        )

        binding.rvHome.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = homeAdapter
        }
    }

    /**
     * Observes data from the ViewModel and updates the UI accordingly.
     */
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe Featured Songs
            launch {
                viewModel.featuredSongs.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            updateSections()
                        }

                        is Resource.Failure -> {
                            Toast.makeText(
                                context,
                                "Error: ${resource.exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is Resource.Loading -> {
                            // Show loading indicator if needed
                        }
                    }
                }
            }

            // Observe Top Artists
            launch {
                viewModel.topArtists.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            updateSections()
                        }

                        is Resource.Failure -> {
                            Toast.makeText(
                                context,
                                "Error loading artists: ${resource.exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is Resource.Loading -> {}
                    }
                }
            }

            // Observe Featured Albums
            launch {
                viewModel.featuredAlbums.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            updateSections()
                        }

                        is Resource.Failure -> {
                            Toast.makeText(
                                context,
                                "Error loading albums: ${resource.exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is Resource.Loading -> {}
                    }
                }
            }

            // Observe Recently Heard
            launch {
                viewModel.recentlyHeard.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            updateSections()
                        }

                        is Resource.Failure -> {
                            Toast.makeText(
                                context,
                                "Error loading recently heard: ${resource.exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is Resource.Loading -> {}
                    }
                }
            }

            // Observe Suggestions
            launch {
                viewModel.suggestions.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            updateSections()
                        }

                        is Resource.Failure -> {
                            Toast.makeText(
                                context,
                                "Error loading suggestions: ${resource.exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is Resource.Loading -> {}
                    }
                }
            }
        }
    }

    /**
     * Updates the sections in the adapter based on the latest data from the ViewModel.
     */
    private fun updateSections() {
        val sections = mutableListOf<HomeSection<*>>()

        // 1. Featured Songs (5 items)
        val featuredSongs = (viewModel.featuredSongs.value as? Resource.Success)?.result
        if (!featuredSongs.isNullOrEmpty()) {
            sections.add(
                HomeSection(
                    title = "Featured song",
                    sectionType = SectionType.FEATURED_SONGS,
                    items = featuredSongs.take(DEFAULT_ITEM_DISPLAY_VALUE)
                )
            )
        }

        // 2. Top Artists
        val topArtists = (viewModel.topArtists.value as? Resource.Success)?.result
        if (!topArtists.isNullOrEmpty()) {
            sections.add(
                HomeSection(
                    title = "Top Artists",
                    sectionType = SectionType.TOP_ARTISTS,
                    items = topArtists
                )
            )
        }

        // 3. Featured Albums
        val featuredAlbums = (viewModel.featuredAlbums.value as? Resource.Success)?.result
        if (!featuredAlbums.isNullOrEmpty()) {
            sections.add(
                HomeSection(
                    title = "Featured albums",
                    sectionType = SectionType.FEATURED_ALBUMS,
                    items = featuredAlbums
                )
            )
        }

        // 4. Recently Heard
        val recentlyHeard = (viewModel.recentlyHeard.value as? Resource.Success)?.result
        if (!recentlyHeard.isNullOrEmpty()) {
            sections.add(
                HomeSection(
                    title = "Recently Heard",
                    sectionType = SectionType.RECENTLY_HEARD,
                    items = recentlyHeard.take(DEFAULT_ITEM_DISPLAY_VALUE)
                )
            )
        }

        // 5. Suggestions for you
        val suggestions = (viewModel.suggestions.value as? Resource.Success)?.result
        if (!suggestions.isNullOrEmpty()) {
            sections.add(
                HomeSection(
                    title = "Suggestions for you",
                    sectionType = SectionType.SUGGESTIONS,
                    items = suggestions.take(DEFAULT_ITEM_DISPLAY_VALUE)
                )
            )
        }

        homeAdapter.submitSections(sections)
    }

    companion object {
        const val DEFAULT_NULL_INT_VALUE = 0

        const val DEFAULT_ITEM_DISPLAY_VALUE = 5
    }
}
