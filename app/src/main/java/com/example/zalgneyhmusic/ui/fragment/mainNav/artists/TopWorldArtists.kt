package com.example.zalgneyhmusic.ui.fragment.mainNav.artists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.databinding.FragmentArtistListBinding
import com.example.zalgneyhmusic.ui.adapter.ArtistAdapter
import com.example.zalgneyhmusic.ui.extension.openArtistDetail
import com.example.zalgneyhmusic.ui.fragment.BaseFragment
import com.example.zalgneyhmusic.ui.viewmodel.fragment.ArtistViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Top World Artists Fragment
 */
@AndroidEntryPoint
class TopWorldArtists : BaseFragment() {

    private var _binding: FragmentArtistListBinding? = null
    private val binding get() = _binding!!

    private val artistViewModel: ArtistViewModel by viewModels()
    private lateinit var artistAdapter: ArtistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupRecyclerView()
        observeArtists()
    }

    private fun setupUI() {
        binding.txtSectionTitle.text = getString(R.string.top_world_artist_of_the_month)
    }

    private fun setupRecyclerView() {
        artistAdapter = ArtistAdapter(
            onArtistClick = { artist ->
                openArtistDetail(artist.id)
            },
            onMenuClick = { artist ->
                mediaActionHandler.onArtistMenuClick(artist)
            }
        )

        binding.rvArtists.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = artistAdapter
        }
    }

    private fun observeArtists() {
        artistViewModel.topWorldArtists.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvArtists.visibility = View.GONE
                    binding.txtError.visibility = View.GONE
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvArtists.visibility = View.VISIBLE
                    binding.txtError.visibility = View.GONE
                    artistAdapter.submitList(resource.result)
                }

                is Resource.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvArtists.visibility = View.GONE
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