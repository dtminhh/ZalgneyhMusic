package com.example.zalgneyhmusic.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.databinding.ItemArtistBinding

/**
 * Adapter display list of Artists
 */
class ArtistAdapter(
    private val onArtistClick: (Artist) -> Unit,
    private val onMenuClick: (Artist) -> Unit
) : ListAdapter<Artist, ArtistAdapter.ArtistViewHolder>(ArtistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ItemArtistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArtistViewHolder(binding, onArtistClick, onMenuClick)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ArtistViewHolder(
        private val binding: ItemArtistBinding,
        private val onArtistClick: (Artist) -> Unit,
        private val onMenuClick: (Artist) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: Artist) {
            binding.apply {
                // Set artist info
                txtArtistName.text = artist.name
                txtArtistInfo.text = itemView.context.getString(R.string.see_more)

                // Load avatar
                Glide.with(imgArtistAvatar.context)
                    .load(artist.imageUrl)
                    .placeholder(R.drawable.ic_album_placeholder)
                    .centerCrop()
                    .into(imgArtistAvatar)

                // Click listeners
                root.setOnClickListener {
                    onArtistClick(artist)
                }

                btnMenu.setOnClickListener {
                    onMenuClick(artist)
                }
            }
        }
    }

    class ArtistDiffCallback : DiffUtil.ItemCallback<Artist>() {
        override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem == newItem
        }
    }
}