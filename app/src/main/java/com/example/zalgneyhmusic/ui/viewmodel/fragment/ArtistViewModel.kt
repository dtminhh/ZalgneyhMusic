package com.example.zalgneyhmusic.ui.viewmodel.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import com.example.zalgneyhmusic.ui.viewmodel.BaseViewModel
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
) : BaseViewModel() {

    private val _topWorldArtists = MutableLiveData<Resource<List<Artist>>>()
    val topWorldArtists: LiveData<Resource<List<Artist>>> = _topWorldArtists

    private val _topCountryArtists = MutableLiveData<Resource<List<Artist>>>()
    val topCountryArtists: LiveData<Resource<List<Artist>>> = _topCountryArtists

    private val _artistDetail = MutableLiveData<Resource<Artist>>()
    val artistDetail: LiveData<Resource<Artist>> = _artistDetail

    private val _artistAlbums = MutableLiveData<Resource<List<Album>>>()
    val artistAlbums: LiveData<Resource<List<Album>>> = _artistAlbums

    private val _artistSongs = MutableLiveData<Resource<List<Song>>>()
    val artistSongs: LiveData<Resource<List<Song>>> = _artistSongs

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
                    is Resource.Success -> _topCountryArtists.value =
                        Resource.Success(resource.result.take(10))

                    is Resource.Loading -> _topCountryArtists.value = Resource.Loading
                    is Resource.Failure -> _topCountryArtists.value = resource
                }
            }
        }
    }

    fun loadArtistDetail(id: String) {
        viewModelScope.launch {
            _artistDetail.value = Resource.Loading
            _artistDetail.value = musicRepository.getArtistById(id)

            // Load Album
            _artistAlbums.value = Resource.Loading
            _artistAlbums.value = musicRepository.getAlbumsByArtist(id)

            // Load songs
            _artistSongs.value = Resource.Loading
            _artistSongs.value = musicRepository.getSongsByArtist(id)
        }
    }
}