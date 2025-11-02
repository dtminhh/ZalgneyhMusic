package com.example.zalgneyhmusic.ui.adapter.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.databinding.HomeItemRecentSuggestBinding
import com.example.zalgneyhmusic.ui.adapter.BaseDiffItemCallback

/**
 * Adapter for Recently Heard and Suggestions sections
 * Displays square thumbnail with play button overlay
 */
class RecentSuggestAdapter(
    private val onItemClick: (Song) -> Unit,
    private val onMoreClick: ((Song) -> Unit)? = null
) : ListAdapter<Song, RecentSuggestAdapter.ViewHolder>(
    BaseDiffItemCallback(
        itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
        contentsTheSame = { oldItem, newItem -> oldItem == newItem }
    )
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HomeItemRecentSuggestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: HomeItemRecentSuggestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            binding.apply {
                txtSongName.text = song.title
                txtArtistName.text = song.artist.name

                Glide.with(itemView.context)
                    .load(song.imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imgThumbnail)

                root.setOnClickListener {
                    onItemClick(song)
                }

                btnMoreOptions.setOnClickListener {
                    onMoreClick?.invoke(song)
                }
            }
        }
    }
}
