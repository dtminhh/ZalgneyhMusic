package com.example.zalgneyhmusic.ui.handler

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.DetailType
import com.example.zalgneyhmusic.data.model.domain.Playlist
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import com.example.zalgneyhmusic.ui.moreoptions.MoreOptionsAction
import com.example.zalgneyhmusic.ui.moreoptions.MoreOptionsManager
import com.example.zalgneyhmusic.ui.navigation.DetailNavigator
import com.example.zalgneyhmusic.ui.viewmodel.fragment.PlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Class that handles ALL menu actions (3 dots) and Play music.
 * Helps Fragment not to repeat handleAction code.
 */
class MediaActionHandler(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val playerViewModel: PlayerViewModel,
    private val navigator: DetailNavigator?,
    private val musicRepository: MusicRepository,
    private val scope: CoroutineScope
) {

    // ==================== SONG ====================
    fun onSongMenuClick(song: Song) {
        MoreOptionsManager.showForSong(fragmentManager, song) { action ->
            handleSongAction(action, song)
        }
    }

    fun onSongClick(song: Song, playlist: List<Song> = listOf(song)) {
        val index = playlist.indexOfFirst { it.id == song.id }.takeIf { it != -1 } ?: 0
        playerViewModel.setPlaylist(playlist, index)
    }

    private fun handleSongAction(action: MoreOptionsAction.SongAction, song: Song) {
        when (action) {
            is MoreOptionsAction.SongAction.PlayNext -> {
                playerViewModel.addSongToNext(song)
                Toast.makeText(context, "Đã thêm vào danh sách phát kế tiếp", Toast.LENGTH_SHORT)
                    .show()
            }

            is MoreOptionsAction.SongAction.AddToQueue -> {
                playerViewModel.addSongToQueue(song)
                Toast.makeText(context, "Đã thêm vào hàng chờ", Toast.LENGTH_SHORT).show()
            }

            is MoreOptionsAction.SongAction.AddToPlaylist -> {
                // Show Add to playlist dialog
            }

            is MoreOptionsAction.SongAction.GoToArtist -> {
                navigator?.navigatorToDetailScreen(DetailType.Artist(song.artist.id))
            }

            is MoreOptionsAction.SongAction.GoToAlbum -> {
                song.album?.id?.let { albumId ->
                    navigator?.navigatorToDetailScreen(DetailType.Album(albumId))
                } ?: Toast.makeText(context, "Unknown Album", Toast.LENGTH_SHORT).show()
            }

            is MoreOptionsAction.SongAction.Share -> {
                shareContent(
                    song.title,
                    "Nghe bài hát ${song.title} của ${song.artist.name} tại Zalgneyh Music!"
                )
            }
        }
    }

    // ==================== ALBUM ====================
    fun onAlbumMenuClick(album: Album) {
        MoreOptionsManager.showForAlbum(fragmentManager, album) { action ->
            handleAlbumAction(action, album)
        }
    }

    private fun handleAlbumAction(action: MoreOptionsAction.AlbumAction, album: Album) {
        when (action) {
            is MoreOptionsAction.AlbumAction.PlayAll -> {
                playAlbum(album)
            }

            is MoreOptionsAction.AlbumAction.AddToPlaylist -> {
            }

            is MoreOptionsAction.AlbumAction.GoToArtist -> {
                navigator?.navigatorToDetailScreen(DetailType.Artist(album.artist.id))
            }

            is MoreOptionsAction.AlbumAction.Share -> {
                shareContent(album.title, "Nghe Album ${album.title} cực hay trên Zalgneyh Music!")
            }
        }
    }

    // Play Album
    private fun playAlbum(album: Album) {
        // already have song list
        if (album.songs.isNotEmpty()) {
            playerViewModel.setPlaylist(album.songs, 0)
            Toast.makeText(context, "Đang phát Album: ${album.title}", Toast.LENGTH_SHORT).show()
            return
        }

        // none songs list
        Toast.makeText(context, "Đang tải danh sách bài hát...", Toast.LENGTH_SHORT).show()

        scope.launch {
            when (val result = musicRepository.getAlbumById(album.id)) {
                is Resource.Success -> {
                    val fullAlbum = result.result
                    if (fullAlbum.songs.isNotEmpty()) {
                        playerViewModel.setPlaylist(fullAlbum.songs, 0)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Đang phát: ${fullAlbum.title}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Album này chưa có bài hát nào",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                is Resource.Failure -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Lỗi tải Album: ${result.exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                else -> {}
            }
        }
    }

    // ==================== ARTIST ====================
    fun onArtistMenuClick(artist: Artist) {
        MoreOptionsManager.showForArtist(fragmentManager, artist) { action ->
            handleArtistAction(action, artist)
        }
    }

    private fun handleArtistAction(action: MoreOptionsAction.ArtistAction, artist: Artist) {
        when (action) {
            is MoreOptionsAction.ArtistAction.Follow -> {
                // viewModel.followArtist(artist.id)
                Toast.makeText(context, "Followed ${artist.name}", Toast.LENGTH_SHORT).show()
            }

            is MoreOptionsAction.ArtistAction.PlayAllSongs -> {
                // Play artist top songs
                playArtist(artist)
            }

            is MoreOptionsAction.ArtistAction.Share -> {
                shareContent(artist.name, "Khám phá nghệ sĩ ${artist.name} trên Zalgneyh Music!")
            }
        }
    }

    private fun playArtist(artist: Artist) {
        Toast.makeText(context, "Đang tải bài hát của ${artist.name}...", Toast.LENGTH_SHORT).show()

        scope.launch {
            val result = musicRepository.getSongsByArtist(artist.id)

            withContext(Dispatchers.Main) {
                when (result) {
                    is Resource.Success -> {
                        val songs = result.result
                        if (songs.isNotEmpty()) {
                            playerViewModel.setPlaylist(songs, 0)
                            Toast.makeText(
                                context,
                                "Đang phát nhạc của ${artist.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Nghệ sĩ này chưa có bài hát nào",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    is Resource.Failure -> {
                        Toast.makeText(
                            context,
                            "Lỗi tải nhạc: ${result.exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {}
                }
            }
        }
    }

    // ==================== PLAYLIST ====================
    fun onPlaylistMenuClick(playlist: Playlist) {
        MoreOptionsManager.showForPlaylist(fragmentManager, playlist) { action ->
            handlePlaylistAction(action)
        }
    }

    private fun handlePlaylistAction(action: MoreOptionsAction.PlaylistAction) {
        when (action) {
            is MoreOptionsAction.PlaylistAction.PlayAll -> { /* Play playlist */
            }

            is MoreOptionsAction.PlaylistAction.Edit -> { /* Open Edit screen */
            }

            is MoreOptionsAction.PlaylistAction.Delete -> { /* Show confirm delete dialog */
            }

            is MoreOptionsAction.PlaylistAction.Share -> { /* Share */
            }
        }
    }

    // ==================== HELPER: SHARE ====================
    private fun shareContent(title: String, message: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, message)
        }
        // show share dialog
        context.startActivity(
            Intent.createChooser(shareIntent, "Chia sẻ qua").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}