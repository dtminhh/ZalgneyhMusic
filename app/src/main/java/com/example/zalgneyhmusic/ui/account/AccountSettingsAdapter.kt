package com.example.zalgneyhmusic.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zalgneyhmusic.databinding.ItemAccountSettingsSectionBinding
import com.example.zalgneyhmusic.databinding.ItemAccountSettingsActionBinding

/**
 * Adapter for Account & Settings sections with beautiful Material Design
 * Reuses existing patterns from MoreOptionsAdapter
 */
class AccountSettingsAdapter(
    private val onActionClick: (AccountSettingsAction) -> Unit
) : ListAdapter<AccountSettingsItem, RecyclerView.ViewHolder>(AccountSettingsDiffCallback()) {

    companion object {
        private const val TYPE_SECTION = 0
        private const val TYPE_ACTION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AccountSettingsItem.Section -> TYPE_SECTION
            is AccountSettingsItem.Action -> TYPE_ACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SECTION -> {
                val binding = ItemAccountSettingsSectionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SectionViewHolder(binding)
            }
            else -> {
                val binding = ItemAccountSettingsActionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ActionViewHolder(binding, onActionClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is AccountSettingsItem.Section -> (holder as SectionViewHolder).bind(item)
            is AccountSettingsItem.Action -> (holder as ActionViewHolder).bind(item)
        }
    }

    class SectionViewHolder(
        private val binding: ItemAccountSettingsSectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AccountSettingsItem.Section) {
            binding.txtSectionTitle.setText(item.titleRes)
        }
    }

    class ActionViewHolder(
        private val binding: ItemAccountSettingsActionBinding,
        private val onActionClick: (AccountSettingsAction) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AccountSettingsItem.Action) {
            binding.apply {
                txtActionTitle.setText(item.action.titleRes)
                imgActionIcon.setImageResource(item.action.iconRes)

                // Apply danger styling if needed
                if (item.action.isDanger) {
                    txtActionTitle.setTextColor(
                        binding.root.context.getColor(android.R.color.holo_red_dark)
                    )
                    imgActionIcon.setColorFilter(
                        binding.root.context.getColor(android.R.color.holo_red_dark)
                    )
                }

                root.setOnClickListener { onActionClick(item.action) }
            }
        }
    }
}

/**
 * DiffUtil callback for efficient RecyclerView updates
 */
class AccountSettingsDiffCallback : DiffUtil.ItemCallback<AccountSettingsItem>() {
    override fun areItemsTheSame(
        oldItem: AccountSettingsItem,
        newItem: AccountSettingsItem
    ): Boolean {
        return when {
            oldItem is AccountSettingsItem.Section && newItem is AccountSettingsItem.Section ->
                oldItem.titleRes == newItem.titleRes
            oldItem is AccountSettingsItem.Action && newItem is AccountSettingsItem.Action ->
                oldItem.action == newItem.action
            else -> false
        }
    }

    override fun areContentsTheSame(
        oldItem: AccountSettingsItem,
        newItem: AccountSettingsItem
    ): Boolean = oldItem == newItem
}

/**
 * Sealed class representing items in the list (Section headers or Actions)
 */
sealed class AccountSettingsItem {
    data class Section(val titleRes: Int) : AccountSettingsItem()
    data class Action(val action: AccountSettingsAction) : AccountSettingsItem()
}

