package com.example.zalgneyhmusic.ui.fragment.detail

import ImageUtils
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.Resource
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.databinding.FragmentAlbumDetailBinding
import com.example.zalgneyhmusic.ui.adapter.SongAdapter
import com.example.zalgneyhmusic.ui.fragment.BaseBottomSheetDialogFragment
import com.example.zalgneyhmusic.ui.utils.setupFullHeightBottomSheet
import com.example.zalgneyhmusic.ui.viewmodel.fragment.AlbumViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumDetailBottomSheet : BaseBottomSheetDialogFragment() {

    private var albumId: String? = null
    private val viewModel: AlbumViewModel by viewModels()

    private var _binding: FragmentAlbumDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var songAdapter: SongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use custom theme to support full screen and remove floating
        setStyle(STYLE_NORMAL, R.style.CustomDetailBottomSheetDialogTheme)
        albumId = arguments?.getString(ARG_ALBUM_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup RecyclerView before loading data
        setupRecyclerView()

        // 2. Load data
        albumId?.let { id ->
            viewModel.loadAlbumDetail(id)
        }

        // 3. observer data return
        observeData()
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

        binding.rvAlbumSongs.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeData() {
        viewModel.albumDetail.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    val album = resource.result
                    // Bind Album Data
                    bindAlbumData(album)

                    // Bind song list
                    if (album.songs.isNotEmpty()) {
                        songAdapter.submitList(album.songs)
                        binding.rvAlbumSongs.visibility = View.VISIBLE
                        binding.txtTrackListHeading.visibility = View.VISIBLE
                    } else {
                        binding.rvAlbumSongs.visibility = View.GONE
                        binding.txtTrackListHeading.visibility = View.GONE
                    }
                }

                is Resource.Failure -> {
                    Toast.makeText(
                        context,
                        "Error: ${resource.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindAlbumData(album: Album) {
        binding.apply {
            txtAlbumTitleDetail.text = album.title
            txtArtistNameDetailAlbum.text = album.artist.name
            txtAlbumInfo.text = "${album.releaseYear ?: "Unknown"} • ${album.totalTracks} bài hát"
            ImageUtils.loadImage(imgAlbumCoverDetail, album.imageUrl)
        }
    }

    override fun onStart() {
        super.onStart()
        // Use utility function to setup full height and rounded corners
        setupFullHeightBottomSheet(dialog as? BottomSheetDialog, resources)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ALBUM_ID = "album_id"
        fun newInstance(albumId: String) = AlbumDetailBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_ALBUM_ID, albumId)
            }
        }
    }
}