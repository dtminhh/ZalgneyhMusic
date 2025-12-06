package com.example.zalgneyhmusic.ui.fragment.mainNav.playlist

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Playlist
import com.example.zalgneyhmusic.databinding.FragmentPlaylistsBinding
import com.example.zalgneyhmusic.ui.adapter.PlaylistAdapter
import com.example.zalgneyhmusic.ui.extension.openPlaylistDetail
import com.example.zalgneyhmusic.ui.fragment.BaseFragment
import com.example.zalgneyhmusic.ui.utils.StorageHelper
import com.example.zalgneyhmusic.ui.viewmodel.fragment.PlaylistViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    // Biến hỗ trợ chọn ảnh
    private var selectedImageUri: android.net.Uri? = null
    private var editDialogView: View? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            editDialogView?.findViewById<ImageView>(R.id.imgPlaylistCover)?.setImageURI(it)
        }
    }

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
        // Observe create playlist state
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

    // Dialog nhập tên playlist
    private fun showCreatePlaylistDialog() {
        val input = EditText(context)
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.create_new_playlist))
            .setView(input)
            .setPositiveButton(getString(R.string.create)) { _, _ ->
                val name = input.text.toString()
                if (name.isNotBlank()) {
                    viewModel.createPlaylist(name)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(
            onPlaylistClick = { playlist ->
                openPlaylistDetail(playlist.id)
            },
            onPlaylistLongClick = { playlist ->
                // SỬA LỖI Ở ĐÂY: Truyền đủ 3 tham số
                mediaActionHandler.onPlaylistMenuClick(
                    playlist = playlist,
                    onEditRequest = { target -> showEditPlaylistDialog(target) },
                    onDeleteSuccess = { viewModel.loadMyPlaylists() }
                )
                true
            }
        )

        binding.rvPlaylists.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = playlistAdapter
        }
    }

    private fun showEditPlaylistDialog(playlist: Playlist) {
        selectedImageUri = null
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_playlist, null)
        editDialogView = dialogView

        val edtName = dialogView.findViewById<EditText>(R.id.edtPlaylistName)
        val imgCover = dialogView.findViewById<ImageView>(R.id.imgPlaylistCover)

        edtName.setText(playlist.name)
        ImageUtils.loadImage(imgCover, playlist.imageUrl)

        imgCover.setOnClickListener { pickImageLauncher.launch("image/*") }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sửa Playlist")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val newName = edtName.text.toString()
                if (newName.isNotBlank()) {
                    val imageFile = selectedImageUri?.let { StorageHelper.uriToFile(requireContext(), it) }
                    viewModel.updatePlaylist(playlist.id, newName, imageFile)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
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