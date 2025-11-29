package com.example.zalgneyhmusic.ui.fragment.mainNav.playlist

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.databinding.FragmentPlaylistsBinding
import com.example.zalgneyhmusic.ui.adapter.PlaylistAdapter
import com.example.zalgneyhmusic.ui.fragment.BaseFragment
import com.example.zalgneyhmusic.ui.viewmodel.fragment.PlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment displaying playlists in vertical list
 */
@AndroidEntryPoint
class PlaylistsFragment : BaseFragment() {

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
        // Quan sát trạng thái tạo playlist
        observeCreateState()
        binding.btnCreatePlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }

    private fun observeCreateState() {
        viewModel.createPlaylistState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                Toast.makeText(context, "Tạo playlist thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateState()
            } else if (resource is Resource.Failure) {
                Toast.makeText(context, "Lỗi: ${resource.exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Dialog nhập tên (Code của bạn đã ổn, chỉ cần gọi đúng hàm viewModel)
    private fun showCreatePlaylistDialog() {
        val input = EditText(context)
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.create_new_playlist)) // Nên dùng string resource
            .setView(input)
            .setPositiveButton(getString(R.string.create)) { _, _ ->
                val name = input.text.toString()
                if (name.isNotBlank()) {
                    viewModel.createPlaylist(name) // Gọi hàm tạo
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
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
                mediaActionHandler.onPlaylistMenuClick(playlist)
                true
            }
        )

        binding.rvPlaylists.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = playlistAdapter
        }
    }

    /**
     * Observe playlists LiveData
     */
    private fun observePlaylists() {
        viewModel.userPlaylists.observe(viewLifecycleOwner) { resource ->
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