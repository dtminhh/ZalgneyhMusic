package com.example.zalgneyhmusic.ui.fragment.player

import ImageUtils
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
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
import androidx.core.graphics.toColorInt

/**
 * Player Fragment - Full screen music player
 * Display album art, song info, controls (play/pause, next, previous, shuffle, repeat)
 */
@AndroidEntryPoint
class PlayerFragment : BaseFragment() {

    private lateinit var binding: FragmentPlayerBinding
    private val viewModel: PlayerViewModel by viewModels()
    private var isUserSeeking = false

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        val icon = if (isFavorite) R.drawable.ic_favorite_on else R.drawable.ic_favorite
        binding.btnFavorite.setImageResource(icon)
        binding.btnFavorite.imageAlpha = if (isFavorite) 255 else 153
    }

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

    private fun setupUI() {
        // Close button
        binding.btnClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnQueue.setOnClickListener {
            QueueBottomSheet.newInstance().show(childFragmentManager, "queue_bottom_sheet")
        }

        binding.btnFavorite.setOnClickListener {
            val currentSong = viewModel.currentSong.value
            if (currentSong != null) {
                mediaActionHandler.toggleFavorite(currentSong)
            }
        }

        binding.btnDownload.setOnClickListener {
            val currentSong = viewModel.currentSong.value
            if (currentSong != null) {
                // Gọi hàm download trong ViewModel (bạn cần thêm hàm này vào ViewModel)
                viewModel.toggleDownload(currentSong)
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
            // 1. Observe song details (Title, Artist, Art, Lyrics)
            // REMOVED: The stale download logic from here
            launch {
                viewModel.currentSong.collect { song ->
                    song?.let {
                        binding.tvSongTitle.text = it.title
                        binding.tvArtistName.text = it.artist.name

                        // Load album art
                        ImageUtils.loadImage(binding.imgAlbumArt, it.imageUrl)

                        // Bind lyrics
                        if (it.lyrics.isNullOrBlank()) {
                            binding.lyricsContainer.visibility = View.VISIBLE
                            binding.tvLyricsPreview.text = getString(R.string.no_lyrics_found)
                            val params = binding.tvLyricsPreview.layoutParams
                            params.height = LayoutParams.MATCH_PARENT
                            binding.tvLyricsPreview.layoutParams = params
                            binding.tvLyricsPreview.gravity = Gravity.CENTER
                            binding.tvLyricsPreview.textAlignment = View.TEXT_ALIGNMENT_CENTER
                        } else {
                            binding.lyricsContainer.visibility = View.VISIBLE
                            binding.tvLyricsPreview.text = it.lyrics
                            val params = binding.tvLyricsPreview.layoutParams
                            params.height = LayoutParams.WRAP_CONTENT
                            binding.tvLyricsPreview.layoutParams = params
                            binding.tvLyricsPreview.gravity = Gravity.START
                            binding.tvLyricsPreview.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                        }
                    }
                }
            }

            // 2. Observe REAL-TIME Download Status (This fixes the icon not changing)
            launch {
                viewModel.isCurrentSongDownloaded.collect { isDownloaded ->
                    val icon = if (isDownloaded) R.drawable.ic_download_done else R.drawable.ic_download
                    binding.btnDownload.setImageResource(icon)
                    // Đổi màu
                    val color = if (isDownloaded) android.graphics.Color.parseColor("#4CAF50")
                    else android.graphics.Color.BLACK
                    binding.btnDownload.setColorFilter(color)
                }
            }

            // 3. Observe Toast Messages (This fixes the missing notifications)
            launch {
                viewModel.uiMessage.collect { message ->
                    android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            // 4. Observe playing state
            launch {
                viewModel.isPlaying.collect { isPlaying ->
                    binding.btnPlayPause.setImageResource(
                        if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                    )
                }
            }

            // 5. Observe favorite state
            launch {
                viewModel.isCurrentSongFavorite.collect { isFavorite ->
                    updateFavoriteIcon(isFavorite)
                }
            }

            // 6. Observe shuffle state
            launch {
                viewModel.shuffleMode.collect { isShuffleOn ->
                    binding.btnShuffle.imageAlpha = if (isShuffleOn) 255 else 128
                    binding.btnShuffle.isSelected = isShuffleOn
                }
            }

            // 7. Observe repeat state
            launch {
                viewModel.repeatMode.collect { mode ->
                    binding.btnRepeat.imageAlpha = if (mode == RepeatMode.NONE) 128 else 255
                    binding.btnRepeat.contentDescription = when (mode) {
                        RepeatMode.ONE -> getString(R.string.repeat) + " (one)"
                        RepeatMode.ALL -> getString(R.string.repeat) + " (all)"
                        RepeatMode.NONE -> getString(R.string.repeat)
                    }
                }
            }

            // 8. Observe position
            launch {
                viewModel.currentPosition.collect { position ->
                    if (!isUserSeeking) {
                        val duration = viewModel.duration.value
                        if (duration > 0) {
                            val progress = (position.toFloat() / duration * 100).toInt()
                            binding.seekBar.progress = progress
                        }
                        binding.tvCurrentTime.text = viewModel.formatTime(position)
                        binding.tvDuration.text = viewModel.formatTime(duration)
                    }
                }
            }
        }
    }
}
