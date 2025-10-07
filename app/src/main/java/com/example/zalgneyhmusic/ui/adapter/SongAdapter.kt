package com.example.zalgneyhmusic.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.api.ApiHelper
import com.example.zalgneyhmusic.data.model.Song
import com.example.zalgneyhmusic.databinding.ItemSongBinding

class SongAdapter(
    private val onSongClick: (Song) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SongViewHolder(
        private val binding: ItemSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            binding.apply {
                tvSongTitle.text = song.title
                tvArtistName.text = song.artist.name
                tvDuration.text = ApiHelper.formatDuration(song.duration)

                // Load hình ảnh với Coil
                ivSongCover.load(ApiHelper.getImageUrl(song.imageUrl)) {
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_background)
                    error(R.drawable.ic_launcher_background)
                }

                root.setOnClickListener {
                    onSongClick(song)
                }
            }
        }
    }

    private class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}

