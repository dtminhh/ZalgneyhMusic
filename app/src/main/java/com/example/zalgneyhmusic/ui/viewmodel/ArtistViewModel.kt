package com.example.zalgneyhmusic.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Artists screen
 * Uses MusicRepository to fetch artists from API/Cache (Hybrid)
 */
@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _topWorldArtists = MutableLiveData<Resource<List<Artist>>>()
    val topWorldArtists: LiveData<Resource<List<Artist>>> = _topWorldArtists

    private val _topCountryArtists = MutableLiveData<Resource<List<Artist>>>()
    val topCountryArtists: LiveData<Resource<List<Artist>>> = _topCountryArtists

    init {
        loadTopWorldArtists()
        loadTopCountryArtists()
    }

    fun loadTopWorldArtists() {
        viewModelScope.launch {
            _topWorldArtists.value = Resource.Loading
            musicRepository.getTopArtists(limit = 20).collect { resource ->
                _topWorldArtists.value = resource
            }
        }
    }

    fun loadTopCountryArtists() {
        viewModelScope.launch {
            _topCountryArtists.value = Resource.Loading
            musicRepository.getAllArtists().collect { resource ->
                // Take first 10 for country section
                when (resource) {
                    is Resource.Success -> _topCountryArtists.value = Resource.Success(resource.result.take(10))
                    is Resource.Loading -> _topCountryArtists.value = Resource.Loading
                    is Resource.Failure -> _topCountryArtists.value = resource
                }
            }
        }
    }
}