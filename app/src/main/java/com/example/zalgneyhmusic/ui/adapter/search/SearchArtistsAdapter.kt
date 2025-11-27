package com.example.zalgneyhmusic.ui.adapter.search

import ImageUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.databinding.ItemSearchArtistBinding

/**
 * Adapter cho search artists results
 */
class SearchArtistsAdapter(
    private val onArtistClick: (Artist) -> Unit
) : ListAdapter<Artist, SearchArtistsAdapter.ArtistViewHolder>(ArtistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ItemSearchArtistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArtistViewHolder(
        private val binding: ItemSearchArtistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: Artist) {
            binding.apply {
                tvArtistName.text = artist.name
                ImageUtils.loadImage(imgArtistAvatar, artist.imageUrl)

                root.setOnClickListener {
                    onArtistClick(artist)
                }
            }
        }
    }

    private class ArtistDiffCallback : DiffUtil.ItemCallback<Artist>() {
        override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem == newItem
        }
    }
}
