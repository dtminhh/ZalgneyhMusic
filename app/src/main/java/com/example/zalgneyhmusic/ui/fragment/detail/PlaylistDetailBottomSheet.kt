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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistDetailBottomSheet : BaseBottomSheetDialogFragment() {

    private var playlistId: String? = null
    private val viewModel: PlaylistViewModel by viewModels()

    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var songAdapter: SongAdapter

    private var editingPlaylistId: String? = null
    private var selectedImageUri: android.net.Uri? = null
    private var editDialogView: View? = null
    private val pickImageLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUri = it
                // Update preview image on the edit dialog if it is currently visible
                editDialogView?.findViewById<android.widget.ImageView>(R.id.imgPlaylistCover)
                    ?.setImageURI(it)
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

        binding.imgMoreOpt.setOnClickListener {
            mediaActionHandler.onPlaylistMenuClick(
                playlist = playlist,
                onEditRequest = { targetPlaylist ->
                    // MediaActionHandler requests edit -> open edit dialog in this fragment
                    showEditPlaylistDialog(targetPlaylist)
                },
                onDeleteSuccess = {
                    // Close the bottom sheet once delete is confirmed
                    dismiss()
                }
            )
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

    // Shows the edit dialog (lives in this Fragment to reuse pickImageLauncher)
    private fun showEditPlaylistDialog(playlist: Playlist) {
        editingPlaylistId = playlist.id
        selectedImageUri = null

        // Inflate the dialog layout (reuse create-playlist layout)
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_playlist, null)
        editDialogView = dialogView // Keep reference to update image after selection

        val edtName = dialogView.findViewById<android.widget.EditText>(R.id.edtPlaylistName)
        val imgCover = dialogView.findViewById<android.widget.ImageView>(R.id.imgPlaylistCover)

        // Pre-fill existing data
        edtName.setText(playlist.name)
        ImageUtils.loadImage(imgCover, playlist.imageUrl)

        // Image selection
        imgCover.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sửa Playlist")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val newName = edtName.text.toString()
                if (newName.isNotBlank()) {
                    // Convert URI -> File
                    val imageFile = selectedImageUri?.let { uri ->
                        com.example.zalgneyhmusic.ui.utils.StorageHelper.uriToFile(
                            requireContext(),
                            uri
                        )
                    }
                    // Gọi ViewModel update (Vì ViewModel nằm ở Fragment Scope)
                    viewModel.updatePlaylist(playlist.id, newName, imageFile)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
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
