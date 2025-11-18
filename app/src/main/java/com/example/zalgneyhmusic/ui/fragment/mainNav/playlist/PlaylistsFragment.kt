package com.example.zalgneyhmusic.ui.fragment.mainNav.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Playlist
import com.example.zalgneyhmusic.databinding.FragmentPlaylistsBinding
import com.example.zalgneyhmusic.ui.adapter.PlaylistAdapter
import com.example.zalgneyhmusic.ui.moreoptions.MoreOptionsAction
import com.example.zalgneyhmusic.ui.moreoptions.MoreOptionsManager
import com.example.zalgneyhmusic.ui.viewmodel.fragment.PlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment displaying playlists in vertical list
 */
@AndroidEntryPoint
class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistViewModel by viewModels()
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observePlaylists()
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(
            onPlaylistClick = { playlist ->
                // TODO: Navigate to playlist detail screen
                Toast.makeText(
                    context,
                    getString(
                        R.string.toast_playlist,
                        "${playlist.name} (${playlist.songs.size} songs)"
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onPlaylistLongClick = { playlist ->
                showPlaylistMoreOptions(playlist)
                true
            }
        )

        binding.rvPlaylists.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = playlistAdapter
        }
    }

    private fun showPlaylistMoreOptions(playlist: Playlist) {
        MoreOptionsManager.showForPlaylist(
            fragmentManager = childFragmentManager,
            playlist = playlist,
            onActionClick = { action ->
                handlePlaylistAction(action, playlist)
            }
        )
    }

    private fun handlePlaylistAction(action: MoreOptionsAction.PlaylistAction, playlist: Playlist) {
        when (action) {
            is MoreOptionsAction.PlaylistAction.PlayAll -> {
                Toast.makeText(context, "Play all: ${playlist.name}", Toast.LENGTH_SHORT).show()
            }
            is MoreOptionsAction.PlaylistAction.Edit -> {
                Toast.makeText(context, "Edit: ${playlist.name}", Toast.LENGTH_SHORT).show()
            }
            is MoreOptionsAction.PlaylistAction.Delete -> {
                Toast.makeText(context, "Delete: ${playlist.name}", Toast.LENGTH_SHORT).show()
            }
            is MoreOptionsAction.PlaylistAction.Share -> {
                Toast.makeText(context, "Share: ${playlist.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Observe playlists LiveData
     */
    private fun observePlaylists() {
        viewModel.playlists.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvPlaylists.visibility = View.GONE
                    binding.txtError.visibility = View.GONE
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvPlaylists.visibility = View.VISIBLE
                    binding.txtError.visibility = View.GONE
                    playlistAdapter.submitList(resource.result)
                }

                is Resource.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvPlaylists.visibility = View.GONE
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