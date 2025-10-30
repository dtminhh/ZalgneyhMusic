package com.example.zalgneyhmusic.ui.moreoptions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.databinding.BottomSheetMoreOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Beautiful Material Design Bottom Sheet for More Options
 */
class MoreOptionsBottomSheet<T : MoreOptionsAction> : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMoreOptionsBinding? = null
    private val binding get() = _binding!!

    private var itemTitle: String = ""
    private var itemSubtitle: String = ""
    private var itemImageUrl: String = ""
    private var actions: List<T> = emptyList()
    private var onActionSelected: ((T) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetMoreOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding.apply {
            txtItemTitle.text = itemTitle
            txtItemSubtitle.text = itemSubtitle

            Glide.with(this@MoreOptionsBottomSheet)
                .load(itemImageUrl)
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .centerCrop()
                .into(imgItemThumbnail)

            rvOptions.layoutManager = LinearLayoutManager(context)
            rvOptions.adapter = MoreOptionsAdapter(actions) { action ->
                onActionSelected?.invoke(action)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun <T : MoreOptionsAction> forSong(
            title: String,
            subtitle: String,
            imageUrl: String,
            actions: List<T>,
            onActionSelected: (T) -> Unit
        ) = MoreOptionsBottomSheet<T>().apply {
            this.itemTitle = title
            this.itemSubtitle = subtitle
            this.itemImageUrl = imageUrl
            this.actions = actions
            this.onActionSelected = onActionSelected
        }

        fun <T : MoreOptionsAction> forArtist(
            title: String,
            subtitle: String,
            imageUrl: String,
            actions: List<T>,
            onActionSelected: (T) -> Unit
        ) = MoreOptionsBottomSheet<T>().apply {
            this.itemTitle = title
            this.itemSubtitle = subtitle
            this.itemImageUrl = imageUrl
            this.actions = actions
            this.onActionSelected = onActionSelected
        }

        fun <T : MoreOptionsAction> forAlbum(
            title: String,
            subtitle: String,
            imageUrl: String,
            actions: List<T>,
            onActionSelected: (T) -> Unit
        ) = MoreOptionsBottomSheet<T>().apply {
            this.itemTitle = title
            this.itemSubtitle = subtitle
            this.itemImageUrl = imageUrl
            this.actions = actions
            this.onActionSelected = onActionSelected
        }

        fun <T : MoreOptionsAction> forPlaylist(
            title: String,
            subtitle: String,
            imageUrl: String,
            actions: List<T>,
            onActionSelected: (T) -> Unit
        ) = MoreOptionsBottomSheet<T>().apply {
            this.itemTitle = title
            this.itemSubtitle = subtitle
            this.itemImageUrl = imageUrl
            this.actions = actions
            this.onActionSelected = onActionSelected
        }
    }
}

