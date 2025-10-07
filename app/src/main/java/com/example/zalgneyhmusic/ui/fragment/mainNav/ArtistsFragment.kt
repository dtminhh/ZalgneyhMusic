package com.example.zalgneyhmusic.ui.fragment.mainNav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zalgneyhmusic.databinding.FragmentArtistsBinding
import com.example.zalgneyhmusic.ui.fragment.BaseNavFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.artists.TopCountryArtists
import com.example.zalgneyhmusic.ui.fragment.mainNav.artists.TopWorldArtists

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
    ): View? {
        _binding = FragmentArtistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabLayoutAndViewPager()
    }

    override fun setupTabLayoutAndViewPager() {
        val tabs = listOf("Top World Artists", "Top Your Country Artists")
        val fragments = listOf(
            { TopWorldArtists() },
            { TopCountryArtists() }
        )

        setupTabsAndFragments(
            viewPager = binding.viewPagerArtists,
            tabs = tabs,
            fragments = fragments
        )
    }
}