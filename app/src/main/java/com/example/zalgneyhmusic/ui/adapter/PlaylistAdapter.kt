package com.example.zalgneyhmusic.ui.adapter

import ImageUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.domain.Playlist
import com.example.zalgneyhmusic.databinding.ItemPlaylistBinding

/**
 * RecyclerView adapter for displaying playlists
 */
class PlaylistAdapter(
    private val onPlaylistClick: (Playlist) -> Unit,
    private val onPlaylistLongClick: ((Playlist) -> Boolean)? = null
) : ListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding, onPlaylistClick, onPlaylistLongClick)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PlaylistViewHolder(
        private val binding: ItemPlaylistBinding,
        private val onPlaylistClick: (Playlist) -> Unit,
        private val onPlaylistLongClick: ((Playlist) -> Boolean)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.apply {
                txtPlaylistName.text = playlist.name
                txtPlaylistDescription.text =
                    playlist.description ?: itemView.context.getString(R.string.no_description)
                txtSongCount.text =
                    itemView.context.getString(R.string.song_count, playlist.songs.size)
                txtCreatedBy.text =
                    itemView.context.getString(R.string.created_by, playlist.createdBy)
                ImageUtils.loadImage(imgPlaylistCover, playlist.imageUrl)

                root.setOnClickListener { onPlaylistClick(playlist) }
                root.setOnLongClickListener { onPlaylistLongClick?.invoke(playlist) ?: false }
            }
        }
    }

    class PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }
    }
}

