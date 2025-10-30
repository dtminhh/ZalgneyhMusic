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
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.databinding.FragmentSongListBinding
import com.example.zalgneyhmusic.ui.adapter.SongAdapter
import com.example.zalgneyhmusic.ui.moreoptions.MoreOptionsAction
import com.example.zalgneyhmusic.ui.moreoptions.MoreOptionsManager
import com.example.zalgneyhmusic.ui.viewmodel.PlayerViewModel
import com.example.zalgneyhmusic.ui.viewmodel.SongViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Feature Songs Fragment - Displays featured songs
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
                Toast.makeText(context, getString(R.string.toast_song, song.title), Toast.LENGTH_SHORT).show()
            },
            onPlayClick = { song ->
                // Get all songs from current list
                val songs = songAdapter.currentList
                val index = songs.indexOf(song)

                // Set playlist and play
                playerViewModel.setPlaylist(songs, index)

                Toast.makeText(context, getString(R.string.toast_playing, song.title), Toast.LENGTH_SHORT).show()
            },
            onMenuClick = { song ->
                showMoreOptions(song)
            }
        )

        binding.rvSongs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
        }
    }

    private fun showMoreOptions(song: com.example.zalgneyhmusic.data.model.domain.Song) {
        MoreOptionsManager.showForSong(
            fragmentManager = childFragmentManager,
            song = song,
            onActionClick = { action ->
                handleSongAction(action, song)
            }
        )
    }

    private fun handleSongAction(action: MoreOptionsAction.SongAction, song: com.example.zalgneyhmusic.data.model.domain.Song) {
        when (action) {
            is MoreOptionsAction.SongAction.PlayNext -> {
                Toast.makeText(context, "Play next: ${song.title}", Toast.LENGTH_SHORT).show()
                // TODO: Implement play next
            }
            is MoreOptionsAction.SongAction.AddToQueue -> {
                Toast.makeText(context, "Added to queue: ${song.title}", Toast.LENGTH_SHORT).show()
                // TODO: Implement add to queue
            }
            is MoreOptionsAction.SongAction.AddToPlaylist -> {
                Toast.makeText(context, "Add to playlist: ${song.title}", Toast.LENGTH_SHORT).show()
                // TODO: Show playlist selection dialog
            }
            is MoreOptionsAction.SongAction.GoToArtist -> {
                Toast.makeText(context, "Go to artist: ${song.artist.name}", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to artist detail
            }
            is MoreOptionsAction.SongAction.GoToAlbum -> {
                Toast.makeText(context, "Go to album: ${song.album?.title ?: "Unknown"}", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to album detail
            }
            is MoreOptionsAction.SongAction.Share -> {
                Toast.makeText(context, "Share: ${song.title}", Toast.LENGTH_SHORT).show()
                // TODO: Implement share functionality
            }
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
                    binding.txtError.text = resource.exception.message ?: getString(R.string.unknown_error)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
