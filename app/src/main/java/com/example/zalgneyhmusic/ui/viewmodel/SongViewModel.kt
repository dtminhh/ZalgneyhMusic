package com.example.zalgneyhmusic.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Songs Screen
 * Uses MusicRepository to fetch songs from API/Cache (Hybrid)
 */
@HiltViewModel
class SongViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _featureSongs = MutableLiveData<Resource<List<Song>>>()
    val featureSongs: LiveData<Resource<List<Song>>> = _featureSongs

    private val _recentSongs = MutableLiveData<Resource<List<Song>>>()
    val recentSongs: LiveData<Resource<List<Song>>> = _recentSongs

    private val _newSongs = MutableLiveData<Resource<List<Song>>>()
    val newSongs: LiveData<Resource<List<Song>>> = _newSongs

    init {
        loadFeatureSongs()
        loadRecentSongs()
        loadNewSongs()
    }

    fun loadFeatureSongs() {
        viewModelScope.launch {
            _featureSongs.value = Resource.Loading
            // Feature songs: reuse top songs (limit 20 for this screen)
            musicRepository.getTopSongs(limit = 20).collect { resource ->
                _featureSongs.value = resource
            }
        }
    }

    fun loadRecentSongs() {
        viewModelScope.launch {
            _recentSongs.value = Resource.Loading
            musicRepository.getRecentSongs(limit = 20).collect { resource ->
                _recentSongs.value = resource
            }
        }
    }

    fun loadNewSongs() {
        viewModelScope.launch {
            _newSongs.value = Resource.Loading
            musicRepository.getNewSongs(limit = 20).collect { resource ->
                _newSongs.value = resource
            }
        }
    }
}
