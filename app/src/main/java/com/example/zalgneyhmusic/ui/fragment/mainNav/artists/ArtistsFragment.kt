package com.example.zalgneyhmusic.ui.fragment.mainNav.artists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zalgneyhmusic.databinding.FragmentArtistsBinding
import com.example.zalgneyhmusic.ui.fragment.BaseNavFragment

/**
 * Artists fragment displaying list of music artists and performers.
 */
class ArtistsFragment : BaseNavFragment() {
    private var _binding: FragmentArtistsBinding? = null
    private val binding get() = _binding!!

    /**
     * Creates and returns the fragment view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabLayoutAndViewPager()
    }

    override fun setupTabLayoutAndViewPager() {
        val tabs = listOf("Top World Artists", "Followed Artists")
        val fragments = listOf(
            { TopWorldArtists() },
            { FollowedArtists() }
        )

        setupTabsAndFragments(
            viewPager = binding.viewPagerArtists,
            tabs = tabs,
            fragments = fragments
        )
    }
}