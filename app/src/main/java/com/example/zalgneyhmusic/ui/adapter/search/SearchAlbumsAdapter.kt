package com.example.zalgneyhmusic.ui.adapter.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.databinding.ItemSearchAlbumBinding

/**
 * Adapter cho search albums results
 */
class SearchAlbumsAdapter(
    private val onAlbumClick: (Album) -> Unit
) : ListAdapter<Album, SearchAlbumsAdapter.AlbumViewHolder>(AlbumDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemSearchAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlbumViewHolder(
        private val binding: ItemSearchAlbumBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {
            binding.apply {
                tvAlbumTitle.text = album.title
                tvArtistName.text = album.artist.name

                // Use unified image property
                val imageToLoad = album.image
                Glide.with(itemView.context)
                    .load(imageToLoad)
                    .placeholder(R.drawable.ic_album)
                    .error(R.drawable.ic_album)
                    .into(imgAlbumArt)

                root.setOnClickListener { onAlbumClick(album) }
            }
        }
    }

    private class AlbumDiffCallback : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem == newItem
        }
    }
}
