package com.example.zalgneyhmusic.ui.adapter.search

import ImageUtils
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.local.entity.SearchHistoryEntity
import com.example.zalgneyhmusic.databinding.ItemRecentSearchBinding
import com.google.android.material.color.MaterialColors

class RecentSearchesAdapter(
    private val onItemClick: (SearchHistoryEntity) -> Unit,
    private val onRemoveClick: (SearchHistoryEntity) -> Unit
) : ListAdapter<SearchHistoryEntity, RecentSearchesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentSearchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemRecentSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchHistoryEntity) {
            binding.apply {
                txtContent.text = item.title

                if (!item.subtitle.isNullOrEmpty()) {
                    txtSubtitle.text = item.subtitle
                    txtSubtitle.visibility = View.VISIBLE
                } else {
                    txtSubtitle.visibility = View.GONE
                }

                if (item.type == "QUERY") {
                    // 1. ICON MODE (Text Search)
                    imgIcon.setImageResource(R.drawable.ic_history)

                    // Restore tint color from theme attributes
                    val tintColor = MaterialColors.getColor(
                        root,
                        com.google.android.material.R.attr.colorOnSurfaceVariant
                    )
                    imgIcon.imageTintList = ColorStateList.valueOf(tintColor)

                    // Use compact scaling for a cleaner icon appearance
                    imgIcon.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE

                } else {
                    // 2. IMAGE MODE (Song / Artist / Album)

                    // Important: remove any tint to preserve original image colors
                    imgIcon.imageTintList = null

                    // Scale image to fill the shape (circle/square)
                    imgIcon.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    ImageUtils.loadImage(imgIcon, item.imageUrl)

                }

                root.setOnClickListener { onItemClick(item) }
                btnRemove.setOnClickListener { onRemoveClick(item) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SearchHistoryEntity>() {
        override fun areItemsTheSame(oldItem: SearchHistoryEntity, newItem: SearchHistoryEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: SearchHistoryEntity,
            newItem: SearchHistoryEntity
        ) =
            oldItem == newItem
    }
}