package com.example.zalgneyhmusic.ui.adapter

import ImageUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.databinding.ItemAlbumBinding

/**
 * RecyclerView adapter for displaying albums in grid layout
 * Uses ListAdapter for automatic diff calculation
 */
class AlbumAdapter(
    private val onAlbumClick: (Album) -> Unit,
    private val onAlbumLongClick: ((Album) -> Boolean)? = null
) : ListAdapter<Album, AlbumAdapter.AlbumViewHolder>(AlbumDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlbumViewHolder(binding, onAlbumClick, onAlbumLongClick)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /** ViewHolder for album item */
    class AlbumViewHolder(
        private val binding: ItemAlbumBinding,
        private val onAlbumClick: (Album) -> Unit,
        private val onAlbumLongClick: ((Album) -> Boolean)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {
            binding.apply {
                txtAlbumTitle.text = album.title
                txtArtistName.text = album.artist.name  // ✅ Display artist name
                txtTrackCount.text =
                    itemView.context.getString(R.string.track_count, album.totalTracks)
                ImageUtils.loadImage(imgAlbumCover, album.image)

                root.setOnClickListener { onAlbumClick(album) }
                root.setOnLongClickListener { onAlbumLongClick?.invoke(album) ?: false }
            }
        }
    }

    /** DiffUtil callback for efficient list updates */
    class AlbumDiffCallback : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean =
            oldItem == newItem
    }
}