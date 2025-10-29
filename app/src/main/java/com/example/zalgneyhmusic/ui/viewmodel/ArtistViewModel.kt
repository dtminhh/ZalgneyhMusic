package com.example.zalgneyhmusic.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Artist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Artists screen (Top World & Top Country)
 */
@HiltViewModel
class ArtistViewModel @Inject constructor(
    // TODO: Inject ArtistRepository when available
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
            try {
                delay(COROUTINE_DELAY_VALUE)
                val demoArtists = getDemoArtists()
                _topWorldArtists.value = Resource.Success(demoArtists)
            } catch (e: Exception) {
                _topWorldArtists.value = Resource.Failure(e)
            }
        }
    }

    fun loadTopCountryArtists() {
        viewModelScope.launch {
            _topCountryArtists.value = Resource.Loading
            try {
                delay(COROUTINE_DELAY_VALUE)
                val demoArtists = getDemoArtists().take(ARTISTS_LIMIT)
                _topCountryArtists.value = Resource.Success(demoArtists)
            } catch (e: Exception) {
                _topCountryArtists.value = Resource.Failure(e)
            }
        }
    }

    /**
     * Demo data artists
     */
    private fun getDemoArtists(): List<Artist> {
        return listOf(
            Artist(
                id = "1",
                name = "Sơn Tùng M-TP",
                bio = "Vietnamese pop star",
                imageUrl = "https://i.scdn.co/image/ab6761610000e5eb4df3645b0467107295100167",
                followers = 5000000,
                verified = true
            ),
            Artist(
                id = "2",
                name = "Hồ Ngọc Hà",
                bio = "Vietnamese pop diva",
                imageUrl = "https://avatar-ex-swe.nixcdn.com/singer/avatar/2018/03/14/5/1/1/5_1521014555699_500.jpg",
                followers = 3000000,
                verified = true
            ),
            Artist(
                id = "3",
                name = "Noo Phước Thịnh",
                bio = "Vietnamese singer",
                imageUrl = "https://avatar-ex-swe.nixcdn.com/singer/avatar/2017/03/21/1/7/7/5/1490084055778_500.jpg",
                followers = 2500000,
                verified = true
            ),
            Artist(
                id = "4",
                name = "Đen Vâu",
                bio = "Vietnamese rapper",
                imageUrl = "https://avatar-ex-swe.nixcdn.com/singer/avatar/2020/05/08/a/3/9/3/1588907090613_500.jpg",
                followers = 4000000,
                verified = true
            ),
            Artist(
                id = "5",
                name = "Bích Phương",
                bio = "Vietnamese singer",
                imageUrl = "https://avatar-ex-swe.nixcdn.com/singer/avatar/2020/12/03/a/3/9/3/1606982442655_500.jpg",
                followers = 2000000,
                verified = true
            ),
            Artist(
                id = "6",
                name = "Mỹ Tâm",
                bio = "Vietnamese pop queen",
                imageUrl = "https://avatar-ex-swe.nixcdn.com/singer/avatar/2017/03/21/1/7/7/5/1490084055778_500.jpg",
                followers = 3500000,
                verified = true
            ),
            Artist(
                id = "7",
                name = "Đức Phúc",
                bio = "Vietnamese idol",
                imageUrl = "https://avatar-ex-swe.nixcdn.com/singer/avatar/2019/08/08/a/3/9/3/1565254140838_500.jpg",
                followers = 1500000,
                verified = true
            ),
            Artist(
                id = "8",
                name = "Erik",
                bio = "Vietnamese singer",
                imageUrl = "https://avatar-ex-swe.nixcdn.com/singer/avatar/2019/12/20/a/3/9/3/1576826452009_500.jpg",
                followers = 1800000,
                verified = true
            )
        )
    }

    companion object {
        const val COROUTINE_DELAY_VALUE = 1000L
        const val ARTISTS_LIMIT = 6
    }
}