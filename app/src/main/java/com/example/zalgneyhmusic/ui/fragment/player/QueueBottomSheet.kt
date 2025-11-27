package com.example.zalgneyhmusic.ui.fragment.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.databinding.BottomsheetQueueBinding
import com.example.zalgneyhmusic.ui.adapter.SongAdapter
import com.example.zalgneyhmusic.ui.fragment.BaseBottomSheetDialogFragment
import com.example.zalgneyhmusic.ui.viewmodel.fragment.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QueueBottomSheet : BaseBottomSheetDialogFragment() {

    private var _binding: BottomsheetQueueBinding? = null
    private val binding get() = _binding!!

    // Shared PlayerViewModel with PlayerFragment (renamed to avoid hiding base member)
    private val sharedPlayerViewModel: PlayerViewModel by activityViewModels()

    private lateinit var nowPlayingAdapter: SongAdapter
    private lateinit var upNextAdapter: SongAdapter
    private lateinit var laterAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nowPlayingAdapter = createQueueAdapter()
        upNextAdapter = createQueueAdapter()
        laterAdapter = createQueueAdapter()

        binding.recyclerNowPlaying.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = nowPlayingAdapter
        }

        binding.recyclerUpNext.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = upNextAdapter
        }

        binding.recyclerLater.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = laterAdapter
        }

        // Observe playlist and current song to split into sections
        viewLifecycleOwner.lifecycleScope.launch {
            sharedPlayerViewModel.playlist.collectLatest { songs ->
                val currentSong = sharedPlayerViewModel.currentSong.value
                if (songs.isEmpty() || currentSong == null) {
                    nowPlayingAdapter.submitList(emptyList())
                    upNextAdapter.submitList(emptyList())
                    laterAdapter.submitList(emptyList())
                    return@collectLatest
                }

                val currentIndex = songs.indexOfFirst { it.id == currentSong.id }
                val safeIndex = if (currentIndex >= 0) currentIndex else 0

                val nowPlayingList = listOf(songs[safeIndex])
                val remaining = songs.drop(safeIndex + 1)

                val upNextList = remaining.take(1)
                val laterList = if (remaining.size > 1) remaining.drop(1) else emptyList()

                nowPlayingAdapter.submitList(nowPlayingList)
                upNextAdapter.submitList(upNextList)
                laterAdapter.submitList(laterList)
            }
        }
    }

    private fun createQueueAdapter(): SongAdapter {
        return SongAdapter(
            onSongClick = { song ->
                val currentList = sharedPlayerViewModel.playlist.value
                val index = currentList.indexOfFirst { it.id == song.id }
                if (index != -1) {
                    sharedPlayerViewModel.setPlaylist(currentList, index)
                }
                dismiss()
            },
            onPlayClick = { song ->
                val currentList = sharedPlayerViewModel.playlist.value
                val index = currentList.indexOfFirst { it.id == song.id }
                if (index != -1) {
                    sharedPlayerViewModel.setPlaylist(currentList, index)
                }
                dismiss()
            },
            onMenuClick = { song ->
                mediaActionHandler.onSongMenuClick(song)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = QueueBottomSheet()
    }
}
