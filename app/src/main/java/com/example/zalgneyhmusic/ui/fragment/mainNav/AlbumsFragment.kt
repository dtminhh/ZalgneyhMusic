package com.example.zalgneyhmusic.ui.fragment.mainNav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zalgneyhmusic.databinding.FragmentAlbumsBinding

/**
 * Albums fragment displaying collection of music albums with cover art.
 */
class AlbumsFragment : Fragment() {
    private lateinit var binding: FragmentAlbumsBinding

    /**
     * Creates and returns the fragment view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlbumsBinding.inflate(inflater, container, false)
        return binding.root
    }
}