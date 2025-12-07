package com.example.zalgneyhmusic.ui.fragment.mainNav.album

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.Resource
import com.example.zalgneyhmusic.databinding.FragmentAlbumsBinding
import com.example.zalgneyhmusic.ui.adapter.AlbumAdapter
import com.example.zalgneyhmusic.ui.extension.openAlbumDetail
import com.example.zalgneyhmusic.ui.fragment.BaseFragment
import com.example.zalgneyhmusic.ui.viewmodel.fragment.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment displaying album collection in grid layout
 */
@AndroidEntryPoint
class AlbumsFragment : BaseFragment() {

    private var _binding: FragmentAlbumsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlbumViewModel by viewModels()
    private lateinit var albumAdapter: AlbumAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeAlbums()
    }

    private fun setupRecyclerView() {
        albumAdapter = AlbumAdapter(
            onAlbumClick = { album ->
                openAlbumDetail(album.id)
            },
            onAlbumLongClick = { album ->
                mediaActionHandler.onAlbumMenuClick(album)
                true
            }
        )

        binding.rvAlbums.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = albumAdapter
        }
    }

    /** Observes album data from ViewModel */
    private fun observeAlbums() {
        viewModel.albums.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvAlbums.visibility = View.GONE
                    binding.txtError.visibility = View.GONE
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvAlbums.visibility = View.VISIBLE
                    binding.txtError.visibility = View.GONE
                    albumAdapter.submitList(resource.result)
                }

                is Resource.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvAlbums.visibility = View.GONE
                    binding.txtError.visibility = View.VISIBLE
                    binding.txtError.text = resource.exception.message ?: getString(R.string.unknown_error)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }
}