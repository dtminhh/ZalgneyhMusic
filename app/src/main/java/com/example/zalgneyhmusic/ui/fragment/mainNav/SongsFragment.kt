package com.example.zalgneyhmusic.ui.fragment.mainNav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zalgneyhmusic.databinding.FragmentSongsBinding
import com.example.zalgneyhmusic.ui.fragment.mainNav.songs.RecentSongsFragment
import com.example.zalgneyhmusic.ui.fragment.BaseNavFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.songs.FeatureSongsFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.songs.TopSongsFragment

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
        val tabs = listOf("Feature Songs", "Top Songs", "Recent Songs")
        val fragments = listOf<() -> androidx.fragment.app.Fragment>(
            { FeatureSongsFragment() },
            { TopSongsFragment() },
            { RecentSongsFragment() }
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