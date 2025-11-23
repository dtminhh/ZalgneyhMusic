package com.example.zalgneyhmusic.ui.extension

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.zalgneyhmusic.data.model.domain.DetailType
import com.example.zalgneyhmusic.ui.navigation.DetailNavigator

/**
 * Open Artist details screen
 */
fun Fragment.openArtistDetail(artistId: String) {
    val navigator = activity as? DetailNavigator
    if (navigator != null) {
        navigator.navigatorToDetailScreen(DetailType.Artist(artistId))
    } else {
        Toast.makeText(context, "Lỗi: Activity không hỗ trợ điều hướng", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Open Album details screen
 */
fun Fragment.openAlbumDetail(albumId: String) {
    val navigator = activity as? DetailNavigator
    if (navigator != null) {
        navigator.navigatorToDetailScreen(DetailType.Album(albumId))
    } else {
        Toast.makeText(context, "Lỗi: Activity không hỗ trợ điều hướng", Toast.LENGTH_SHORT).show()
    }
}