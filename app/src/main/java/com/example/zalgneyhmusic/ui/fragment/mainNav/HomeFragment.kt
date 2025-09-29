package com.example.zalgneyhmusic.ui.fragment.mainNav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zalgneyhmusic.databinding.FragmentHomeBinding

/**
 * Home fragment displaying main landing page with music recommendations.
 */
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    /**
     * Creates and returns the fragment view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
}