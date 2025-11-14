package com.example.zalgneyhmusic.ui.adapter.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.databinding.HomeItemFeatureAlbumsBinding
import com.example.zalgneyhmusic.ui.adapter.BaseDiffItemCallback

/**
 * Adapter for Featured Albums list
 * Displays albums with cover image, title, and artist name
 */
class FeaturedAlbumsAdapter(
    private val onItemClick: (Album) -> Unit,
    private val onMoreClick: ((Album) -> Unit)? = null
) : ListAdapter<Album, FeaturedAlbumsAdapter.ViewHolder>(
    BaseDiffItemCallback(
        itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
        contentsTheSame = { oldItem, newItem -> oldItem == newItem }
    )
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HomeItemFeatureAlbumsBinding.inflate(
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
        private val binding: HomeItemFeatureAlbumsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {
            binding.apply {
                txtAlbumName.text = album.title
                txtArtistName.text = album.artist.name

                // Use unified image property
                val imageToLoad = album.image
                Glide.with(itemView.context)
                    .load(imageToLoad)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imgAlbumCover)

                root.setOnClickListener { onItemClick(album) }
                btnMoreOptions.setOnClickListener { onMoreClick?.invoke(album) }
            }
        }
    }
}
