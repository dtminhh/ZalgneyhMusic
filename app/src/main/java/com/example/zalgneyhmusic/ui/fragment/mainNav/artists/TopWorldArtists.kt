package com.example.zalgneyhmusic.ui.fragment.mainNav.artists

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class TopWorldArtists : Fragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return TextView(requireContext()).apply {
            text = "Feature Artist Content"
            textSize = 18f
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
        }
    }
}