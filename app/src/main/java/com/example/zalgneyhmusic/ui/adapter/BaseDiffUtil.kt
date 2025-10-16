package com.example.zalgneyhmusic.ui.adapter

import androidx.recyclerview.widget.DiffUtil.Callback

class BaseDiffUtil<T>(
    private val oldList: List<T>,
    private val newList: List<T>,
    val areItemsTheSame: (T, T) -> Boolean,
    val areContentsTheSame: (T, T) -> Boolean
) : Callback() {
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