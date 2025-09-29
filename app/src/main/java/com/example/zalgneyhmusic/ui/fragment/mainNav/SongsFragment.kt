package com.example.zalgneyhmusic.ui.fragment.mainNav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zalgneyhmusic.databinding.FragmentSongsBinding

/**
 * Songs fragment displaying list of all available songs with search functionality.
 */
class SongsFragment : Fragment() {
    private lateinit var binding: FragmentSongsBinding

    /**
     * Creates and returns the fragment view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }
}