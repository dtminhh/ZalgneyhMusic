package com.example.zalgneyhmusic.ui.adapter.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.local.entity.SearchHistoryEntity
import com.example.zalgneyhmusic.databinding.ItemRecentSearchBinding

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

                // Hiển thị subtitle (nếu có)
                if (!item.subtitle.isNullOrEmpty()) {
                    txtSubtitle.text = item.subtitle
                    txtSubtitle.visibility = View.VISIBLE
                } else {
                    txtSubtitle.visibility = View.GONE
                }

                // Xử lý hình ảnh icon
                if (item.type == "QUERY") {
                    // Nếu là từ khóa tìm kiếm -> Hiện icon kính lúp mặc định
                    imgIcon.setImageResource(R.drawable.ic_history) // Hoặc ic_search
                    imgIcon.clearColorFilter()
                } else {
                    // Nếu là Bài hát/Artist -> Load ảnh từ URL
                    Glide.with(root)
                        .load(item.imageUrl)
                        .placeholder(R.drawable.ic_music_note)
                        .error(R.drawable.ic_music_note)
                        .circleCrop() // Hoặc rounded corners tùy design
                        .into(imgIcon)
                }

                root.setOnClickListener { onItemClick(item) }
                btnRemove.setOnClickListener { onRemoveClick(item) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SearchHistoryEntity>() {
        override fun areItemsTheSame(oldItem: SearchHistoryEntity, newItem: SearchHistoryEntity) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SearchHistoryEntity, newItem: SearchHistoryEntity) =
            oldItem == newItem
    }
}