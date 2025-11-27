package com.example.zalgneyhmusic.ui.adapter.home

import ImageUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
                ImageUtils.loadImage(imgThumbnail, song.imageUrl)


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
