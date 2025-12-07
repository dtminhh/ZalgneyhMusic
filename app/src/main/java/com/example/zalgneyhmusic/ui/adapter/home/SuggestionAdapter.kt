package com.example.zalgneyhmusic.ui.adapter.home

import ImageUtils
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.databinding.ItemSuggestionSongBinding

class SuggestionAdapter(
    private val onSongClick: (Song) -> Unit,
    private val onPlayClick: (Song) -> Unit,
    private val onMenuClick: (Song) -> Unit
) : ListAdapter<Song, SuggestionAdapter.SuggestionViewHolder>(SuggestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        // Note: Create layout file item_suggestion_song.xml or use HomeItemFeatureSongBinding and find views by ID.
        // Here we assume ItemSuggestionSongBinding is generated from the layout.
        val binding = ItemSuggestionSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SuggestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class SuggestionViewHolder(
        private val binding: ItemSuggestionSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(song: Song, position: Int) {
            binding.apply {
                tvTitle.text = song.title
                tvArtist.text = song.artist.name
                ImageUtils.loadImage(ivThumbnail, song.imageUrl)

                // --- Simulated match score display logic for demo ---
                // Because the list is sorted descending in the repository,
                // assume the first item is 99%, subsequent items decrease slightly.
                val baseScore = 99
                val drop = position * 2 // Each rank decreases 2%
                val score = (baseScore - drop).coerceAtLeast(60) // Minimum 60%

                tvMatchScore.text = "$score% Match"

                // Change color based on match to emphasize top items
                if (position < 3) {
                    tvMatchScore.setTextColor(ContextCompat.getColor(root.context, R.color.center_color_main_app_color)) // Primary color
                    tvMatchScore.text = "$score% Match 🔥" // Add fire icon for top 3
                } else {
                    tvMatchScore.setTextColor(ContextCompat.getColor(root.context, R.color.gray))
                }
                // -----------------------------------------------

                root.setOnClickListener { onSongClick(song) }
                btnPlay.setOnClickListener { onPlayClick(song) }
                btnMenu.setOnClickListener { onMenuClick(song) }
            }
        }
    }

    class SuggestionDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}