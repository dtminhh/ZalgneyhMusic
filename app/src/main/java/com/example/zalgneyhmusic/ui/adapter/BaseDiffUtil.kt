package com.example.zalgneyhmusic.ui.adapter

import androidx.recyclerview.widget.DiffUtil

/**
 * Base DiffUtil Callback for manual DiffUtil calculations
 * Used when you need fine-grained control over diff calculations
 */
@Suppress("unused") // Available for custom RecyclerView.Adapter implementations
class BaseDiffUtil<T>(
    private val oldList: List<T>,
    private val newList: List<T>,
    val areItemsTheSame: (T, T) -> Boolean,
    val areContentsTheSame: (T, T) -> Boolean
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])
    }
}

/**
 * Base DiffUtil ItemCallback for ListAdapter
 * Simplifies DiffUtil.ItemCallback creation with lambda functions
 *
 * Usage example:
 * ```kotlin
 * class MyAdapter : ListAdapter<MyItem, ViewHolder>(
 *     BaseDiffItemCallback(
 *         areItemsTheSame = { old, new -> old.id == new.id },
 *         areContentsTheSame = { old, new -> old == new }
 *     )
 * )
 * ```
 */
class BaseDiffItemCallback<T>(
    private val itemsTheSame: (T, T) -> Boolean,
    private val contentsTheSame: (T, T) -> Boolean
) : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
        return itemsTheSame(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
        return contentsTheSame(oldItem, newItem)
    }
}