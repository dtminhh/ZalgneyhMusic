package com.example.zalgneyhmusic.ui.adapter

import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.zalgneyhmusic.ui.fragment.BaseFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.AlbumsFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.ArtistsFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.HomeFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.PlaylistsFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.SongsFragment

/**
 * Adapter managing fragments in the main ViewPager2 navigation.
 */
class MainFragmentAdapter(fragment: BaseFragment) : FragmentStateAdapter(fragment) {
    private val listMainFragment = listOf(
        HomeFragment(),
        SongsFragment(),
        AlbumsFragment(),
        ArtistsFragment(),
        PlaylistsFragment()
    )

    /**
     * Creates fragment for given position.
     */
    override fun createFragment(position: Int) = listMainFragment[position]

    /**
     * Returns total fragment count.
     */
    override fun getItemCount() = listMainFragment.size
}