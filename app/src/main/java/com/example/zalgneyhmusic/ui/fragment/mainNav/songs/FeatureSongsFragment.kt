package com.example.zalgneyhmusic.ui.fragment.mainNav.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class FeatureSongsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TextView(requireContext()).apply {
            textSize = 18f
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
        }
    }
}