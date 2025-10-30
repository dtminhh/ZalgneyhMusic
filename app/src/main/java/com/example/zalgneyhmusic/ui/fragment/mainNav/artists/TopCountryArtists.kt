package com.example.zalgneyhmusic.ui.fragment.mainNav.artists

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
import com.example.zalgneyhmusic.databinding.FragmentArtistListBinding
import com.example.zalgneyhmusic.ui.adapter.ArtistAdapter
import com.example.zalgneyhmusic.ui.moreoptions.MoreOptionsAction
import com.example.zalgneyhmusic.ui.moreoptions.MoreOptionsManager
import com.example.zalgneyhmusic.ui.viewmodel.ArtistViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Top Country Artists Fragment
 */
@AndroidEntryPoint
class TopCountryArtists : Fragment() {

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
        binding.txtSectionTitle.text = getString(R.string.top_artist_in_your_country)
    }

    private fun setupRecyclerView() {
        artistAdapter = ArtistAdapter(
            onArtistClick = { artist ->
                Toast.makeText(context, "Artist: ${artist.name}", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to artist detail
            },
            onMenuClick = { artist ->
                showArtistMoreOptions(artist)
            }
        )

        binding.rvArtists.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = artistAdapter
        }
    }

    private fun showArtistMoreOptions(artist: com.example.zalgneyhmusic.data.model.domain.Artist) {
        MoreOptionsManager.showForArtist(
            fragmentManager = childFragmentManager,
            artist = artist,
            onActionClick = { action ->
                handleArtistAction(action, artist)
            }
        )
    }

    private fun handleArtistAction(action: MoreOptionsAction.ArtistAction, artist: com.example.zalgneyhmusic.data.model.domain.Artist) {
        when (action) {
            is MoreOptionsAction.ArtistAction.Follow -> {
                Toast.makeText(context, "Follow: ${artist.name}", Toast.LENGTH_SHORT).show()
            }
            is MoreOptionsAction.ArtistAction.PlayAllSongs -> {
                Toast.makeText(context, "Play all by: ${artist.name}", Toast.LENGTH_SHORT).show()
            }
            is MoreOptionsAction.ArtistAction.Share -> {
                Toast.makeText(context, "Share: ${artist.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeArtists() {
        artistViewModel.topCountryArtists.observe(viewLifecycleOwner) { resource ->
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