package com.example.zalgneyhmusic.ui.fragment.detail

import ImageUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.databinding.FragmentArtistDetailBinding
import com.example.zalgneyhmusic.ui.adapter.AlbumAdapter
import com.example.zalgneyhmusic.ui.adapter.SongAdapter
import com.example.zalgneyhmusic.ui.extension.openAlbumDetail
import com.example.zalgneyhmusic.ui.fragment.BaseBottomSheetDialogFragment
import com.example.zalgneyhmusic.ui.utils.setupFullHeightBottomSheet
import com.example.zalgneyhmusic.ui.viewmodel.fragment.ArtistViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArtistDetailBottomSheet : BaseBottomSheetDialogFragment() {
    private var artistId: String? = null
    private val viewModel: ArtistViewModel by viewModels()
    private var _binding: FragmentArtistDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var albumAdapter: AlbumAdapter
    private lateinit var songAdapter: SongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        artistId = arguments?.getString(ARG_ARTIST_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        artistId?.let { id ->
            viewModel.loadArtistDetail(id)
        }
        observeData()
    }

    private fun observeData() {
        // 1. Artist Info
        viewModel.artistDetail.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                bindArtistData(resource.result)
            }
        }

        // 2. Albums List
        viewModel.artistAlbums.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                val albums = resource.result
                if (albums.isNotEmpty()) {
                    albumAdapter.submitList(albums)
                    binding.txtAlbumsHeading.visibility = View.VISIBLE
                    binding.rvArtistAlbums.visibility = View.VISIBLE
                } else {
                    binding.txtAlbumsHeading.visibility = View.GONE
                    binding.rvArtistAlbums.visibility = View.GONE
                }
            }
        }

        // 3. Songs List
        viewModel.artistSongs.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                val songs = resource.result
                if (songs.isNotEmpty()) {
                    songAdapter.submitList(songs)
                    binding.txtPopularSongsHeading.visibility = View.VISIBLE
                    binding.rvArtistPopularSongs.visibility = View.VISIBLE
                } else {
                    binding.txtPopularSongsHeading.visibility = View.GONE
                    binding.rvArtistPopularSongs.visibility = View.GONE
                }
            }
        }
    }

    private fun bindArtistData(artist: Artist) {
        binding.apply {
            txtArtistNameDetail.text = artist.name
            txtFollowersCount.text = "${artist.followers} người theo dõi"

            // Load thumbnail
            ImageUtils.loadImage(imgArtistAvatarDetail, artist.imageUrl)
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

    private fun setupRecyclerViews() {
        // 1. Setup Album RecyclerView (Horizontal)
        albumAdapter = AlbumAdapter(
            onAlbumClick = { album ->
                openAlbumDetail(album.id)
            }
        )
        binding.rvArtistAlbums.apply {
            adapter = albumAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // 2. Setup Song RecyclerView (Vertical)
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
        binding.rvArtistPopularSongs.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }


    companion object {
        private const val ARG_ARTIST_ID = "artist_id"
        fun newInstance(artistId: String) = ArtistDetailBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_ARTIST_ID, artistId)
            }
        }
    }
}