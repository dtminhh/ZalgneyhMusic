package com.example.zalgneyhmusic.ui.fragment.mainNav.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.zalgneyhmusic.databinding.FragmentSongsBinding
import com.example.zalgneyhmusic.ui.fragment.BaseNavFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.songs.vpSongFragment.DownloadedSongsFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.songs.vpSongFragment.FeatureSongsFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.songs.vpSongFragment.NewSongsFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.songs.vpSongFragment.SuggestionFragment

/**
 * Songs fragment with TabLayout and child ViewPager2
 */
class SongsFragment : BaseNavFragment() {

    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabLayoutAndViewPager()
    }

    override fun setupTabLayoutAndViewPager() {
        val tabs = listOf("Feature", "New Songs", "Suggestion", "Downloaded")
        val fragments = listOf<() -> Fragment>(
            { FeatureSongsFragment() },
            { NewSongsFragment() },
            { SuggestionFragment() },
            { DownloadedSongsFragment() }
        )

        setupTabsAndFragments(
            viewPager = binding.viewPagerSongs,
            tabs = tabs,
            fragments = fragments
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}