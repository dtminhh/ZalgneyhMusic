package com.example.zalgneyhmusic.ui.fragment.mainNav.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.databinding.FragmentSongListBinding
import com.example.zalgneyhmusic.ui.adapter.SongAdapter
import com.example.zalgneyhmusic.ui.viewmodel.PlayerViewModel
import com.example.zalgneyhmusic.ui.viewmodel.SongViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Feature Songs Fragment - Hiển thị các bài hát nổi bật
 */
@AndroidEntryPoint
class FeatureSongsFragment : Fragment() {

    private var _binding: FragmentSongListBinding? = null
    private val binding get() = _binding!!

    private val songViewModel: SongViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by activityViewModels()
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
        observeSongs()
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            onSongClick = { song ->
                Toast.makeText(context, "Song: ${song.title}", Toast.LENGTH_SHORT).show()
            },
            onPlayClick = { song ->
                // Get all songs from current list
                val songs = songAdapter.currentList
                val index = songs.indexOf(song)

                // Set playlist và play
                playerViewModel.setPlaylist(songs, index)

                Toast.makeText(context, "Playing: ${song.title}", Toast.LENGTH_SHORT).show()
            },
            onMenuClick = { song ->
                Toast.makeText(context, "Menu: ${song.title}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvSongs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
        }
    }

    private fun observeSongs() {
        songViewModel.featureSongs.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvSongs.visibility = View.GONE
                    binding.txtError.visibility = View.GONE
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvSongs.visibility = View.VISIBLE
                    binding.txtError.visibility = View.GONE
                    songAdapter.submitList(resource.result)
                }

                is Resource.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvSongs.visibility = View.GONE
                    binding.txtError.visibility = View.VISIBLE
                    binding.txtError.text = resource.exception.message ?: "Unknown error"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
