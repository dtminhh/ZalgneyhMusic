package com.example.zalgneyhmusic.ui.fragment.mainNav.songs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class TopSongsFragment : Fragment() {
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TextView(requireContext()).apply {
            text = "Top Songs Content"
            textSize = 18f
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
        }
    }
}