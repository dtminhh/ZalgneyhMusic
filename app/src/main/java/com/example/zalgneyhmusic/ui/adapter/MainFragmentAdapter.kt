package com.example.zalgneyhmusic.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.zalgneyhmusic.ui.fragment.mainNav.album.AlbumsFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.artists.ArtistsFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.HomeFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.playlist.PlaylistsFragment
import com.example.zalgneyhmusic.ui.fragment.mainNav.songs.SongsFragment

/**
 * Adapter managing fragments in the main ViewPager2 navigation.
 */
class MainFragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
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