package com.example.zalgneyhmusic.ui.handler

import android.content.Context
import android.widget.Toast
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.ui.moreoptions.MoreOptionsAction

object SongActionHandler {

    fun handleSongAction(context: Context, action: MoreOptionsAction.SongAction, song: Song) {
        when (action) {
            is MoreOptionsAction.SongAction.PlayNext -> {
                Toast.makeText(context, "Play next: ${song.title}", Toast.LENGTH_SHORT).show()
            }

            is MoreOptionsAction.SongAction.AddToQueue -> {
                Toast.makeText(context, "Added to queue: ${song.title}", Toast.LENGTH_SHORT).show()
            }

            is MoreOptionsAction.SongAction.AddToPlaylist -> {
                Toast.makeText(context, "Add to playlist: ${song.title}", Toast.LENGTH_SHORT).show()
            }

            is MoreOptionsAction.SongAction.GoToArtist -> {
                Toast.makeText(context, "Go to artist: ${song.artist.name}", Toast.LENGTH_SHORT)
                    .show()
            }

            is MoreOptionsAction.SongAction.GoToAlbum -> {
                Toast.makeText(
                    context,
                    "Go to album: ${song.album?.title ?: "Unknown"}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is MoreOptionsAction.SongAction.Share -> {
                Toast.makeText(context, "Share: ${song.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}