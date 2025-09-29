package com.example.zalgneyhmusic.ui.fragment.mainNav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zalgneyhmusic.databinding.FragmentPlaylistsBinding

/**
 * Playlists fragment displaying user-created and curated music playlists.
 */
class PlaylistsFragment : Fragment() {
    private lateinit var binding: FragmentPlaylistsBinding

    /**
     * Creates and returns the fragment view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }
}