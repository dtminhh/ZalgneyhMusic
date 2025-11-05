package com.example.zalgneyhmusic.ui.fragment

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.databinding.FragmentMainBinding
import com.example.zalgneyhmusic.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.launch

/**
 * Extension functions for MainFragment to handle mini player functionality
 */

/**
 * Setup mini player UI and observers
 */
fun MainFragment.setupMiniPlayerExt(binding: FragmentMainBinding) {
    val playerViewModel: PlayerViewModel by activityViewModels()

    binding.miniPlayer.apply {
        // Setup click listeners
        miniPlayerContainer.setOnClickListener {
            // Navigate to full player screen
            try {
                findNavController().navigate(R.id.action_mainFragment_to_playerFragment)
            } catch (e: Exception) {
                // Handle navigation error silently
            }
        }

        btnMiniPlayPause.setOnClickListener {
            playerViewModel.togglePlayPause()
        }

        btnMiniNext.setOnClickListener {
            playerViewModel.next()
        }

        btnMiniPrevious.setOnClickListener {
            playerViewModel.previous()
        }


        // Observe player state
        lifecycleScope.launch {
            // Observe current song
            launch {
                playerViewModel.currentSong.collect { song ->
                    if (song != null) {
                        // Show mini player
                        miniPlayerContainer.visibility = View.VISIBLE

                        // Update song info
                        tvMiniSongTitle.text = song.title
                        tvMiniArtistName.text = song.artist.name

                        // Load album art
                        Glide.with(this@setupMiniPlayerExt)
                            .load(song.imageUrl)
                            .placeholder(R.drawable.ic_music_note)
                            .error(R.drawable.ic_music_note)
                            .into(imgMiniAlbumArt)
                    } else {
                        // Hide mini player if no song
                        miniPlayerContainer.visibility = View.GONE
                    }
                }
            }

            // Observe playing state
            launch {
                playerViewModel.isPlaying.collect { isPlaying ->
                    btnMiniPlayPause.setImageResource(
                        if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                    )
                }
            }
        }
    }
}