package com.example.zalgneyhmusic.ui.fragment.mainNav.songs.vpSongFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.databinding.FragmentSongListBinding
import com.example.zalgneyhmusic.ui.adapter.SongAdapter
import com.example.zalgneyhmusic.ui.fragment.BaseFragment
import com.example.zalgneyhmusic.ui.viewmodel.fragment.SongViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadedSongsFragment : BaseFragment() {

    private var _binding: FragmentSongListBinding? = null
    private val binding get() = _binding!!

    // Use shared or dedicated ViewModel depending on app architecture; using SongViewModel here.
    private val viewModel: SongViewModel by viewModels()
    private lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        // Provide required callbacks to the adapter
        songAdapter = SongAdapter(
            onSongClick = { song ->
                val currentList = songAdapter.currentList
                mediaActionHandler.onSongClick(song, currentList)
            },
            onPlayClick = { song ->
                val currentList = songAdapter.currentList
                mediaActionHandler.onSongClick(song, currentList)
            },
            onMenuClick = { song ->
                mediaActionHandler.onSongMenuClick(song)
            }
        )

        binding.rvSongs.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeData() {
        viewModel.downloadedSongs.observe(viewLifecycleOwner) { songs ->
            songAdapter.submitList(songs)
            // Optionally show an empty-state message when the list is empty
            // e.g. binding.txtError.text = getString(R.string.no_downloaded_songs)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}