package com.example.zalgneyhmusic.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.databinding.ItemAlbumBinding

/**
 * RecyclerView adapter for displaying albums in grid layout
 * Uses ListAdapter for automatic diff calculation
 */
class AlbumAdapter(
    private val onAlbumClick: (Album) -> Unit
) : ListAdapter<Album, AlbumAdapter.AlbumViewHolder>(AlbumDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlbumViewHolder(binding, onAlbumClick)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /** ViewHolder for album item */
    class AlbumViewHolder(
        private val binding: ItemAlbumBinding,
        private val onAlbumClick: (Album) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {
            binding.apply {
                txtAlbumTitle.text = album.title
                txtArtistName.text = album.artist
                txtTrackCount.text = itemView.context.getString(R.string.track_count, album.totalTracks)

                // Load album cover with Glide
                Glide.with(imgAlbumCover.context)
                    .load(album.imageUrl)
                    .placeholder(R.drawable.ic_album_placeholder)
                    .centerCrop()
                    .into(imgAlbumCover)

                root.setOnClickListener { onAlbumClick(album) }
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