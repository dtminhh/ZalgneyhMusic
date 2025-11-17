package com.example.zalgneyhmusic.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.databinding.HomeItemFeatureSongBinding

/**
 * Adapter for displaying list of Songs
 */
class SongAdapter(
    private val onSongClick: (Song) -> Unit,
    private val onPlayClick: (Song) -> Unit,
    private val onMenuClick: (Song) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = HomeItemFeatureSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongViewHolder(binding, onSongClick, onPlayClick, onMenuClick)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SongViewHolder(
        private val binding: HomeItemFeatureSongBinding,
        private val onSongClick: (Song) -> Unit,
        private val onPlayClick: (Song) -> Unit,
        private val onMenuClick: (Song) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            binding.apply {
                // Set song info
                tvTitle.text = song.title
                tvArtist.text = song.artist.name
                tvDuration.text = formatDuration(song.duration)

                // Load thumbnail
                Glide.with(ivThumbnail.context)
                    .load(song.imageUrl)
                    .placeholder(R.drawable.ic_music_note)
                    .error(R.drawable.ic_music_note)
                    .centerCrop()
                    .into(ivThumbnail)

                // Click listeners
                root.setOnClickListener {
                    onSongClick(song)
                }

                btnPlay.setOnClickListener {
                    onPlayClick(song)
                }

                btnMenu.setOnClickListener {
                    onMenuClick(song)
                }
            }
        }

        private fun formatDuration(seconds: Int): String {
            val minutes = seconds / 60
            val secs = seconds % 60
            return String.format("%02d:%02d", minutes, secs)
        }
    }

    class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}