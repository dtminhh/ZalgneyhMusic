package com.example.zalgneyhmusic.ui.fragment.mainNav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zalgneyhmusic.databinding.FragmentArtistsBinding

/**
 * Artists fragment displaying list of music artists and performers.
 */
class ArtistsFragment : Fragment() {
    private lateinit var binding: FragmentArtistsBinding

    /**
     * Creates and returns the fragment view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArtistsBinding.inflate(inflater, container, false)
        return binding.root
    }
}