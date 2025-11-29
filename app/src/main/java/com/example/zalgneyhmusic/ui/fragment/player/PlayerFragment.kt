package com.example.zalgneyhmusic.ui.fragment.player

import ImageUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.databinding.FragmentPlayerBinding
import com.example.zalgneyhmusic.player.RepeatMode
import com.example.zalgneyhmusic.ui.fragment.BaseFragment
import com.example.zalgneyhmusic.ui.viewmodel.fragment.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Player Fragment - Full screen music player
 * Display album art, song info, controls (play/pause, next, previous, shuffle, repeat)
 */
@AndroidEntryPoint
class PlayerFragment : BaseFragment() {

    private lateinit var binding: FragmentPlayerBinding
    private val viewModel: PlayerViewModel by viewModels()
    private var isUserSeeking = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observePlayerState()
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        val icon = if (isFavorite) R.drawable.ic_favorite_on else R.drawable.ic_favorite
        binding.btnFavorite.setImageResource(icon)
    }

    private fun setupUI() {
        // Close button
        binding.btnClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnFavorite.setOnClickListener {
            val currentSong = viewModel.currentSong.value
            if (currentSong != null) {
                mediaActionHandler.toggleFavorite(currentSong)
                updateFavoriteIcon(true)
            }

        }

        // Play/Pause
        binding.btnPlayPause.setOnClickListener {
            viewModel.togglePlayPause()
        }

        // Next
        binding.btnNext.setOnClickListener {
            viewModel.next()
        }

        // Previous
        binding.btnPrevious.setOnClickListener {
            viewModel.previous()
        }

        // Shuffle
        binding.btnShuffle.setOnClickListener {
            viewModel.toggleShuffle()
        }

        // Repeat
        binding.btnRepeat.setOnClickListener {
            viewModel.toggleRepeat()
        }

        // Seekbar
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val duration = viewModel.duration.value
                    val position = (progress / 100f * duration).toInt()
                    binding.tvCurrentTime.text = viewModel.formatTime(position)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                val duration = viewModel.duration.value
                val position = (seekBar!!.progress / 100f * duration).toInt()
                viewModel.seekTo(position)
            }
        })
    }

    private fun observePlayerState() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe current song
            launch {
                viewModel.currentSong.collect { song ->
                    song?.let {
                        binding.tvSongTitle.text = it.title
                        binding.tvArtistName.text = it.artist.name

                        // Load album art
                        ImageUtils.loadImage(binding.imgAlbumArt, it.imageUrl)
                    }
                }
            }

            // Observe playing state
            launch {
                viewModel.isPlaying.collect { isPlaying ->
                    binding.btnPlayPause.setImageResource(
                        if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                    )
                }
            }

            // Observe position
            launch {
                viewModel.currentPosition.collect { position ->
                    if (!isUserSeeking) {
                        val duration = viewModel.duration.value
                        if (duration > 0) {
                            val progress = (position.toFloat() / duration * 100).toInt()
                            binding.seekBar.progress = progress
                        }
                        binding.tvCurrentTime.text = viewModel.formatTime(position)
                    }
                }
            }

            // Observe duration
            launch {
                viewModel.duration.collect { duration ->
                    binding.tvDuration.text = viewModel.formatTime(duration)
                }
            }

            // Observe shuffle mode
            launch {
                viewModel.shuffleMode.collect { isShuffleOn ->
                    binding.btnShuffle.alpha = if (isShuffleOn) 1.0f else 0.5f
                }
            }

            // Observe repeat mode
            launch {
                viewModel.repeatMode.collect { repeatMode ->
                    when (repeatMode) {
                        RepeatMode.NONE -> {
                            binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
                            binding.btnRepeat.alpha = 0.5f
                        }

                        RepeatMode.ONE -> {
                            binding.btnRepeat.setImageResource(R.drawable.ic_repeat_one)
                            binding.btnRepeat.alpha = 1.0f
                        }

                        RepeatMode.ALL -> {
                            binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
                            binding.btnRepeat.alpha = 1.0f
                        }
                    }
                }
            }
            launch {
                viewModel.isCurrentSongFavorite.collect { isFav ->
                    updateFavoriteIcon(isFav)
                }
            }
        }
    }
}