package com.example.zalgneyhmusic.ui.fragment.mainNav.playlist

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Playlist
import com.example.zalgneyhmusic.databinding.DialogCreatePlaylistBinding
import com.example.zalgneyhmusic.databinding.FragmentPlaylistsBinding
import com.example.zalgneyhmusic.ui.adapter.PlaylistAdapter
import com.example.zalgneyhmusic.ui.extension.openPlaylistDetail
import com.example.zalgneyhmusic.ui.fragment.BaseFragment
import com.example.zalgneyhmusic.ui.utils.StorageHelper
import com.example.zalgneyhmusic.ui.viewmodel.fragment.PlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.drawable.toDrawable

/**
 * Fragment displaying playlists in vertical list
 */
@AndroidEntryPoint
class PlaylistsFragment : BaseFragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistViewModel by viewModels()
    private lateinit var playlistAdapter: PlaylistAdapter

    // Image picker support variables
    private var selectedImageUri: android.net.Uri? = null
    private var currentDialogBinding: DialogCreatePlaylistBinding? = null
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUri = it
                currentDialogBinding?.imageView?.setImageURI(it)
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

    /**
     * Observes playlist creation state and shows appropriate messages.
     */
    private fun observeCreateState() {
        viewModel.createPlaylistState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                Toast.makeText(context, getString(R.string.toast_playlist_created), Toast.LENGTH_SHORT).show()
                viewModel.resetCreateState()
            } else if (resource is Resource.Failure) {
                Toast.makeText(context, getString(R.string.error, resource.exception.message), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * Shows dialog to create a new playlist.
     */
    /**
     * Shows dialog to create a new playlist.
     */
    private fun showCreatePlaylistDialog() {
        // Reset previous state
        selectedImageUri = null

        // Inflate layout
        val dialogBinding = DialogCreatePlaylistBinding.inflate(layoutInflater)
        currentDialogBinding = dialogBinding // Save for launcher use

        // Setup Dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        // Handle image selection
        dialogBinding.imageView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnCreate.setOnClickListener {
            val name = dialogBinding.etPlaylistName.text.toString().trim()
            val description = dialogBinding.etDescription.text.toString().trim()

            if (name.isNotEmpty()) {
                // Convert URI to File (if image selected)
                val imageFile = selectedImageUri?.let { uri ->
                    StorageHelper.uriToFile(requireContext(), uri)
                }

                // Call ViewModel to create playlist
                viewModel.createPlaylist(name, description, imageFile)

                dialog.dismiss()
            } else {
                dialogBinding.etPlaylistName.error = getString(R.string.error_name_empty)
            }
        }

        // Clear binding when dialog dismissed to avoid memory leak
        dialog.setOnDismissListener {
            currentDialogBinding = null
        }

        dialog.show()
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(
            onPlaylistClick = { playlist ->
                openPlaylistDetail(playlist.id)
            },
            onPlaylistLongClick = { playlist ->
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

    /**
     * Shows dialog to edit an existing playlist.
     */
    private fun showEditPlaylistDialog(playlist: Playlist) {
        selectedImageUri = null
        val dialogBinding = DialogCreatePlaylistBinding.inflate(layoutInflater)
        currentDialogBinding = dialogBinding // Save for launcher use

        // Fill existing data
        dialogBinding.etPlaylistName.setText(playlist.name)
        dialogBinding.btnCreate.text = getString(R.string.save)

        // Load current playlist image (using your image library, e.g. Glide/Picasso/ImageUtils)
        // ImageUtils.loadImage(dialogBinding.imageView, playlist.imageUrl)

        // Handle image selection
        dialogBinding.imageView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnCreate.setOnClickListener {
            val newName = dialogBinding.etPlaylistName.text.toString().trim()
            if (newName.isNotBlank()) {
                // Convert Uri to File for upload
                val imageFile = selectedImageUri?.let { StorageHelper.uriToFile(requireContext(), it) }
                viewModel.updatePlaylist(playlist.id, newName, imageFile)
                dialog.dismiss()
            }
        }

        dialog.setOnDismissListener {
            currentDialogBinding = null
        }

        dialog.show()
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
                    binding.txtError.text =
                        resource.exception.message ?: getString(R.string.unknown_error)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}