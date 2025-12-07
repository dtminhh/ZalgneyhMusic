package com.example.zalgneyhmusic.ui.fragment.mainNav.songs.vpSongFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.databinding.FragmentSongListBinding
import com.example.zalgneyhmusic.ui.adapter.home.SuggestionAdapter
import com.example.zalgneyhmusic.ui.fragment.BaseFragment
import com.example.zalgneyhmusic.ui.viewmodel.fragment.SongViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SuggestionFragment : BaseFragment() {

    private var _binding: FragmentSongListBinding? = null
    private val binding get() = _binding!!

    private val songViewModel: SongViewModel by viewModels()
    private lateinit var suggestionAdapter: SuggestionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        suggestionAdapter = SuggestionAdapter(
            onSongClick = { song ->
                mediaActionHandler.onSongClick(song, suggestionAdapter.currentList)
            },
            onPlayClick = { song ->
                mediaActionHandler.onSongClick(song, suggestionAdapter.currentList)
            },
            onMenuClick = { song ->
                mediaActionHandler.onSongMenuClick(song)
            }
        )

        binding.rvSongs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = suggestionAdapter
        }
    }

    // Observe suggestions from ViewModel
    // Quan sát suggestions từ ViewModel
    private fun observeData() {
        songViewModel.suggestions.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvSongs.visibility = View.GONE
                    binding.txtError.visibility = View.GONE
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvSongs.visibility = View.VISIBLE
                    binding.txtError.visibility = View.GONE

                    if (resource.result.isEmpty()) {
                        binding.txtError.text = getString(R.string.no_suggestions)
                        binding.txtError.text = "Chưa có gợi ý nào cho bạn"
                    } else {
                        suggestionAdapter.submitList(resource.result)
                    }
                }

                is Resource.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvSongs.visibility = View.GONE
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