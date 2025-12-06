package com.example.zalgneyhmusic.ui.fragment.detail

import ImageUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Playlist
import com.example.zalgneyhmusic.databinding.FragmentPlaylistDetailBinding
import com.example.zalgneyhmusic.ui.adapter.SongAdapter
import com.example.zalgneyhmusic.ui.fragment.BaseBottomSheetDialogFragment
import com.example.zalgneyhmusic.ui.utils.setupFullHeightBottomSheet
import com.example.zalgneyhmusic.ui.viewmodel.fragment.PlaylistViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.zalgneyhmusic.databinding.DialogCreatePlaylistBinding
import android.graphics.Color
import android.app.AlertDialog
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.drawable.toDrawable

@AndroidEntryPoint
class PlaylistDetailBottomSheet : BaseBottomSheetDialogFragment() {

    private var playlistId: String? = null
    private val viewModel: PlaylistViewModel by viewModels()

    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var songAdapter: SongAdapter

    private var editingPlaylistId: String? = null
    private var selectedImageUri: android.net.Uri? = null

    private var editBinding: DialogCreatePlaylistBinding? = null
    private val pickImageLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUri = it
                // Update image preview using binding
                editBinding?.imageView?.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use a custom theme to support full screen and remove floating style
        setStyle(STYLE_NORMAL, R.style.CustomDetailBottomSheetDialogTheme)
        playlistId = arguments?.getString(ARG_PLAYLIST_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()

        // Ensure playlists are loaded so we can resolve the detail
        viewModel.loadMyPlaylists()
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            onSongClick = { song ->
                mediaActionHandler.onSongClick(song, songAdapter.currentList)
            },
            onPlayClick = { song ->
                mediaActionHandler.onSongClick(song, songAdapter.currentList)
            },
            onMenuClick = { song ->
                mediaActionHandler.onSongMenuClick(song)
            }
        )

        binding.rvPlaylistSongs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
        }
    }

    private fun observeData() {
        viewModel.userPlaylists.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Optional: add loading state handling here if needed
                }

                is Resource.Success -> {
                    val playlists = resource.result
                    val target = playlists.firstOrNull { it.id == playlistId }
                    if (target != null) {
                        bindPlaylistData(target)
                    } else {
                        Toast.makeText(
                            context,
                            getString(R.string.unknown_error),
                            Toast.LENGTH_SHORT
                        ).show()
                        dismiss()
                    }
                }

                is Resource.Failure -> {
                    Toast.makeText(
                        context,
                        "Error: ${resource.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }
            }
        }
    }

    private fun bindPlaylistData(playlist: Playlist) {
        binding.txtPlaylistTitleDetail.text = playlist.name

        // Show creator name from domain model
        if (playlist.createdBy.isNotBlank()) {
            binding.txtPlaylistOwner.visibility = View.VISIBLE
            binding.txtPlaylistOwner.text = getString(R.string.created_by, playlist.createdBy)
        } else {
            binding.txtPlaylistOwner.visibility = View.GONE
        }

        binding.txtPlaylistInfo.text = resources.getQuantityString(
            R.plurals.numberOfSongs,
            playlist.songs.size,
            playlist.songs.size
        )

        ImageUtils.loadImage(binding.imgPlaylistCoverDetail, playlist.imageUrl)

        binding.imgMoreOpt.setOnClickListener {
            mediaActionHandler.onPlaylistMenuClick(
                playlist = playlist,
                onEditRequest = { target ->
                    showEditPlaylistDialog(target)
                },
                onDeleteSuccess = {
                    dismiss()
                }
            )
        }

        if (playlist.songs.isNotEmpty()) {
            binding.rvPlaylistSongs.visibility = View.VISIBLE
            binding.txtTrackListHeading.visibility = View.VISIBLE

            // Submit playlist tracks to adapter
            songAdapter.submitList(playlist.songs)
        } else {
            // Hide track list section when the playlist is empty
            binding.rvPlaylistSongs.visibility = View.GONE
            binding.txtTrackListHeading.visibility = View.GONE
        }

    }

    override fun onStart() {
        super.onStart()
        setupFullHeightBottomSheet(dialog as? BottomSheetDialog, resources)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Shows the edit playlist dialog.
     * Default playlists cannot be edited.
     *
     * @param playlist Playlist to edit
     */
    private fun showEditPlaylistDialog(playlist: Playlist) {
        if (playlist.isDefault) {
            Toast.makeText(context, getString(R.string.toast_cannot_edit_default_playlist), Toast.LENGTH_SHORT).show()
            return
        }
        editingPlaylistId = playlist.id
        selectedImageUri = null // Reset selected image

        // Use ViewBinding for dialog to avoid ID conflicts
        editBinding = DialogCreatePlaylistBinding.inflate(layoutInflater)
        val binding = editBinding!! // Non-null shortcut

        // Setup UI for edit mode
        binding.etPlaylistName.setText(playlist.name)

        // Load current image
        ImageUtils.loadImage(binding.imageView, playlist.imageUrl)

        // Change button text for edit context
        binding.btnCreate.text = getString(R.string.save)

        // Handle image selection
        binding.imageView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Create dialog
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(binding.root)
        val dialog = dialogBuilder.create()

        // Make dialog background transparent for rounded corners
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        // Handle button clicks
        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        binding.btnCreate.setOnClickListener {
            val newName = binding.etPlaylistName.text.toString().trim()

            if (newName.isNotBlank()) {
                val imageFile = selectedImageUri?.let { uri ->
                    com.example.zalgneyhmusic.ui.utils.StorageHelper.uriToFile(
                        requireContext(),
                        uri
                    )
                }

                // Call ViewModel to update playlist
                viewModel.updatePlaylist(playlist.id, newName, imageFile)

                dialog.dismiss()
            } else {
                binding.etPlaylistName.error = getString(R.string.error_name_empty)
            }
        }

        dialog.show()
    }

    companion object {
        private const val ARG_PLAYLIST_ID = "playlist_id"
        fun newInstance(playlistId: String) = PlaylistDetailBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_PLAYLIST_ID, playlistId)
            }
        }
    }
}
