package com.example.zalgneyhmusic.ui.adapter.home

import ImageUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.databinding.HomeItemTopArtistsBinding
import com.example.zalgneyhmusic.ui.adapter.BaseDiffItemCallback

/**
 * Adapter for Top Artists list
 * Displays artists in circular avatar with name
 */
class TopArtistsAdapter(
    private val onItemClick: (Artist) -> Unit,
    private val onMoreClick: ((Artist) -> Unit)? = null
) : ListAdapter<Artist, TopArtistsAdapter.ViewHolder>(
    BaseDiffItemCallback(
        itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
        contentsTheSame = { oldItem, newItem -> oldItem == newItem }
    )
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HomeItemTopArtistsBinding.inflate(
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
        private val binding: HomeItemTopArtistsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: Artist) {
            binding.apply {
                txtArtistName.text = artist.name
                ImageUtils.loadImage(imgArtistAva, artist.imageUrl)

                root.setOnClickListener {
                    onItemClick(artist)
                }

                btnMoreOptions.setOnClickListener {
                    onMoreClick?.invoke(artist)
                }
            }
        }
    }
}

