package com.example.zalgneyhmusic.ui.viewmodel.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.model.Resource
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import com.example.zalgneyhmusic.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Albums screen
 * Uses MusicRepository to fetch albums from API/Cache (Hybrid)
 */
@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : BaseViewModel() {

    // Private mutable LiveData for internal updates
    private val _albums = MutableLiveData<Resource<List<Album>>>()

    // Public immutable LiveData for Fragment observation
    val albums: LiveData<Resource<List<Album>>> = _albums

    private val _featuredAlbums = MutableLiveData<Resource<List<Album>>>()

    private val _albumDetail = MutableLiveData<Resource<Album>>()
    val albumDetail: LiveData<Resource<Album>> = _albumDetail

    init {
        loadAlbums()
        loadFeaturedAlbums()
    }

    /** Loads all albums from repository */
    fun loadAlbums() {
        viewModelScope.launch {
            _albums.value = Resource.Loading
            musicRepository.getAllAlbums().collect { resource ->
                _albums.value = resource
            }
        }
    }

    /** Loads featured albums (recent 10) */
    fun loadFeaturedAlbums() {
        viewModelScope.launch {
            _featuredAlbums.value = Resource.Loading
            musicRepository.getRecentAlbums(limit = 10).collect { resource ->
                _featuredAlbums.value = resource
            }
        }
    }

    fun loadAlbumDetail(id: String) {
        viewModelScope.launch {
            _albumDetail.value = Resource.Loading
            val result = musicRepository.getAlbumById(id)
            _albumDetail.value = result
        }
    }
}