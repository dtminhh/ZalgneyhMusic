package com.example.zalgneyhmusic.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel cho Songs
 */
@HiltViewModel
class SongViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val DEMO_DATA_DELAY_MS = 1000L
    }

    private val _featureSongs = MutableLiveData<Resource<List<Song>>>()
    val featureSongs: LiveData<Resource<List<Song>>> = _featureSongs

    private val _topSongs = MutableLiveData<Resource<List<Song>>>()
    val topSongs: LiveData<Resource<List<Song>>> = _topSongs

    private val _recentSongs = MutableLiveData<Resource<List<Song>>>()
    val recentSongs: LiveData<Resource<List<Song>>> = _recentSongs

    init {
        loadFeatureSongs()
        loadTopSongs()
        loadRecentSongs()
    }

    fun loadFeatureSongs() {
        viewModelScope.launch {
            _featureSongs.value = Resource.Loading
            try {
                delay(DEMO_DATA_DELAY_MS)
                val demoSongs = getDemoSongs()
                _featureSongs.value = Resource.Success(demoSongs)
            } catch (e: Exception) {
                _featureSongs.value = Resource.Failure(e)
            }
        }
    }

    fun loadTopSongs() {
        viewModelScope.launch {
            _topSongs.value = Resource.Loading
            try {
                delay(DEMO_DATA_DELAY_MS)
                val demoSongs = getDemoSongs().sortedByDescending { it.plays }
                _topSongs.value = Resource.Success(demoSongs)
            } catch (e: Exception) {
                _topSongs.value = Resource.Failure(e)
            }
        }
    }

    fun loadRecentSongs() {
        viewModelScope.launch {
            _recentSongs.value = Resource.Loading
            try {
                delay(DEMO_DATA_DELAY_MS)
                val demoSongs = getDemoSongs().sortedByDescending { it.createdAt }
                _recentSongs.value = Resource.Success(demoSongs)
            } catch (e: Exception) {
                _recentSongs.value = Resource.Failure(e)
            }
        }
    }

    private fun getDemoSongs(): List<Song> {
        val artist = Artist(
            id = "1",
            name = "Sơn Tùng M-TP",
            imageUrl = "https://i.scdn.co/image/ab6761610000e5eb4df3645b0467107295100167",
            followers = 1000000,
            verified = true
        )

        val album = Album(
            id = "1",
            title = "Sky Tour",
            artist = "Sơn Tùng M-TP",
            releaseYear = 2019,
            imageUrl = "https://i.scdn.co/image/ab67616d0000b273ba5db46f4b838ef6027e6f96",
            totalTracks = 10
        )

        return listOf(
            Song(
                id = "1",
                title = "Chúng ta không thuộc về nhau",
                artist = artist,
                album = album,
                duration = 243,
                url = "https://example.com/song1.mp3",
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273ba5db46f4b838ef6027e6f96",
                releaseDate = "2019-07-01",
                plays = 50000000,
                likes = 1000000
            ),
            Song(
                id = "2",
                title = "Hãy trao cho anh",
                artist = artist,
                album = album,
                duration = 258,
                url = "https://example.com/song2.mp3",
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273ba5db46f4b838ef6027e6f96",
                releaseDate = "2019-01-01",
                plays = 100000000,
                likes = 2000000
            ),
            Song(
                id = "3",
                title = "Lạc trôi",
                artist = artist,
                album = album,
                duration = 265,
                url = "https://example.com/song3.mp3",
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273ba5db46f4b838ef6027e6f96",
                releaseDate = "2017-01-01",
                plays = 200000000,
                likes = 5000000
            ),
            Song(
                id = "4",
                title = "Nơi này có anh",
                artist = artist,
                album = album,
                duration = 270,
                url = "https://example.com/song4.mp3",
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273ba5db46f4b838ef6027e6f96",
                releaseDate = "2018-04-30",
                plays = 150000000,
                likes = 3000000
            ),
            Song(
                id = "5",
                title = "Chạy ngay đi",
                artist = artist,
                album = album,
                duration = 275,
                url = "https://example.com/song5.mp3",
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273ba5db46f4b838ef6027e6f96",
                releaseDate = "2018-07-06",
                plays = 120000000,
                likes = 2500000
            ),
            Song(
                id = "6",
                title = "Có chắc yêu là đây",
                artist = artist,
                album = album,
                duration = 240,
                url = "https://example.com/song6.mp3",
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273ba5db46f4b838ef6027e6f96",
                releaseDate = "2015-07-01",
                plays = 80000000,
                likes = 1500000
            ),
            Song(
                id = "7",
                title = "Em của ngày hôm qua",
                artist = artist,
                album = album,
                duration = 253,
                url = "https://example.com/song7.mp3",
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273ba5db46f4b838ef6027e6f96",
                releaseDate = "2016-07-17",
                plays = 90000000,
                likes = 2000000
            ),
            Song(
                id = "8",
                title = "Muộn rồi mà sao còn",
                artist = artist,
                album = album,
                duration = 260,
                url = "https://example.com/song8.mp3",
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273ba5db46f4b838ef6027e6f96",
                releaseDate = "2023-01-01",
                plays = 30000000,
                likes = 800000
            )
        )
    }
}

