package com.example.zalgneyhmusic.ui.fragment.mainNav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.databinding.FragmentHomeBinding
import com.example.zalgneyhmusic.ui.adapter.home.HomeParentAdapter
import com.example.zalgneyhmusic.ui.extension.openAlbumDetail
import com.example.zalgneyhmusic.ui.extension.openArtistDetail
import com.example.zalgneyhmusic.ui.fragment.BaseFragment
import com.example.zalgneyhmusic.ui.model.HomeSection
import com.example.zalgneyhmusic.ui.model.SectionType
import com.example.zalgneyhmusic.ui.viewmodel.fragment.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Home fragment displaying main landing page with music recommendations.
 * Sections: Featured Songs (5), Top Artists, Featured Albums, Recently Heard, Suggestions
 */
@AndroidEntryPoint
class HomeFragment : BaseFragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
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
                val playlist = when (sectionType) {
                    SectionType.FEATURED_SONGS ->
                        (viewModel.featuredSongs.value as? Resource.Success)?.result

                    SectionType.RECENTLY_HEARD ->
                        (viewModel.recentlyHeard.value as? Resource.Success)?.result

                    SectionType.SUGGESTIONS ->
                        (viewModel.suggestions.value as? Resource.Success)?.result

                    else -> null
                } ?: listOf(song)
                mediaActionHandler.onSongClick(song, playlist)
            },
            onArtistClick = { artist ->
                openArtistDetail(artist.id)
            },
            onAlbumClick = { album ->
                openAlbumDetail(album.id)
            },
            onSongMoreClick = { song ->
                mediaActionHandler.onSongMenuClick(song)
            },
            onArtistMoreClick = { artist ->
                mediaActionHandler.onArtistMenuClick(artist)
            },
            onAlbumMoreClick = { album ->
                mediaActionHandler.onAlbumMenuClick(album)
            },
            onSeeMoreClick = { sectionType ->
                when (sectionType) {
                    SectionType.RECENTLY_HEARD -> {
                        // Navigate to RecentSongsFragment
                        try {
                            findNavController().navigate(R.id.recentSongsFragment)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    SectionType.SUGGESTIONS -> {
                        try {
                            findNavController().navigate(R.id.suggestionFragment)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Chưa cài đặt điều hướng", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {}
                }
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
                                getString(
                                    R.string.error_loading_artists,
                                    resource.exception.message
                                ),
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
                                getString(
                                    R.string.error_loading_albums,
                                    resource.exception.message
                                ),
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
                                getString(
                                    R.string.error_loading_recently_heard,
                                    resource.exception.message
                                ),
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
                                getString(
                                    R.string.error_loading_suggestions,
                                    resource.exception.message
                                ),
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
        const val DEFAULT_ITEM_DISPLAY_VALUE = 5
    }
}
