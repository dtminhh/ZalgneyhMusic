package com.example.zalgneyhmusic.ui.adapter.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.databinding.HomeItemFeatureSongBinding
import com.example.zalgneyhmusic.ui.adapter.BaseDiffItemCallback

/**
 * Adapter for Featured Songs list
 * Displays songs with ranking number, thumbnail, title, and artist
 */
class FeaturedSongsAdapter(
    private val onItemClick: (Song) -> Unit
) : ListAdapter<Song, FeaturedSongsAdapter.ViewHolder>(
    BaseDiffItemCallback(
        itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
        contentsTheSame = { oldItem, newItem -> oldItem == newItem }
    )
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HomeItemFeatureSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ViewHolder(
        private val binding: HomeItemFeatureSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(song: Song, position: Int) {
            binding.apply {
                tvOrder.text = (position + 1).toString()
                tvTitle.text = song.title
                tvArtist.text = song.artist.name
                tvDuration.text = formatDuration(song.duration)

                Glide.with(itemView.context)
                    .load(song.imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivThumbnail)

                root.setOnClickListener {
                    onItemClick(song)
                }
            }
        }

        private fun formatDuration(seconds: Int): String {
            val minutes = seconds / 60
            val secs = seconds % 60
            return String.format(java.util.Locale.US, "%d:%02d", minutes, secs)
        }
    }
}
