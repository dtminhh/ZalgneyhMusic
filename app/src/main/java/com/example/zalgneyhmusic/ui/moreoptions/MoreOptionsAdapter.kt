package com.example.zalgneyhmusic.ui.moreoptions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zalgneyhmusic.databinding.ItemMoreOptionBinding

/**
 * Adapter for More Options list
 */
class MoreOptionsAdapter<T : MoreOptionsAction>(
    private val actions: List<T>,
    private val onActionClick: (T) -> Unit
) : RecyclerView.Adapter<MoreOptionsAdapter<T>.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMoreOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(actions[position])
    }

    override fun getItemCount() = actions.size

    inner class ViewHolder(
        private val binding: ItemMoreOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(action: T) {
            binding.apply {
                txtTitle.setText(action.titleRes)
                imgIcon.setImageResource(action.iconRes)

                root.setOnClickListener {
                    onActionClick(action)
                }
            }
        }
    }
}

