package com.example.zalgneyhmusic.ui.adapter.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zalgneyhmusic.databinding.ItemRecentSearchBinding

/**
 * Adapter cho recent searches
 */
class RecentSearchesAdapter(
    private val onSearchClick: (String) -> Unit,
    private val onRemoveClick: (String) -> Unit
) : ListAdapter<String, RecentSearchesAdapter.RecentSearchViewHolder>(StringDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchViewHolder {
        val binding = ItemRecentSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecentSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentSearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecentSearchViewHolder(
        private val binding: ItemRecentSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(searchQuery: String) {
            binding.apply {
                tvSearchQuery.text = searchQuery

                root.setOnClickListener {
                    onSearchClick(searchQuery)
                }

                btnRemove.setOnClickListener {
                    onRemoveClick(searchQuery)
                }
            }
        }
    }

    private class StringDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
