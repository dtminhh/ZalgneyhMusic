package com.example.zalgneyhmusic.ui.adapter.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zalgneyhmusic.databinding.HomeItemParentBinding
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.ui.adapter.BaseDiffUtil
import com.example.zalgneyhmusic.ui.model.HomeSection
import com.example.zalgneyhmusic.ui.model.SectionType

/**
 * Parent Adapter for Home Screen
 * Manages nested RecyclerView with different section types
 */
class HomeParentAdapter(
    private val onSongClick: (Song, SectionType) -> Unit,
    private val onArtistClick: (Artist) -> Unit,
    private val onAlbumClick: (Album) -> Unit
) : RecyclerView.Adapter<HomeParentAdapter.HomeParentViewHolder>() {

    private val sections = mutableListOf<HomeSection<*>>()

    /**
     * Submits new sections and calculates diff efficiently
     * Uses BaseDiffUtil for optimal RecyclerView updates
     */
    fun submitSections(newSections: List<HomeSection<*>>) {
        val oldSections = sections.toList()

        val diffCallback = BaseDiffUtil(
            oldList = oldSections,
            newList = newSections,
            areItemsTheSame = { old, new ->
                old.sectionType == new.sectionType
            },
            areContentsTheSame = { old, new ->
                old.title == new.title && old.items.size == new.items.size
            }
        )

        val diffResult = DiffUtil.calculateDiff(diffCallback)

        sections.clear()
        sections.addAll(newSections)

        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeParentViewHolder {
        val binding = HomeItemParentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeParentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeParentViewHolder, position: Int) {
        holder.bind(sections[position])
    }

    override fun getItemCount(): Int = sections.size

    inner class HomeParentViewHolder(
        private val binding: HomeItemParentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds section data to child RecyclerView
         * Uses type casting based on SectionType enum - safe because types are validated by enum
         */
        @Suppress("UNCHECKED_CAST")
        fun bind(section: HomeSection<*>) {
            binding.tvSectionTitle.text = section.title

            // Setup child RecyclerView based on section type
            // Type casting is safe here because SectionType enum guarantees correct types
            when (section.sectionType) {
                SectionType.FEATURED_SONGS -> {
                    setupFeaturedSongsRecyclerView(section as HomeSection<Song>)
                }
                SectionType.TOP_ARTISTS -> {
                    setupArtistsRecyclerView(section as HomeSection<Artist>)
                }
                SectionType.FEATURED_ALBUMS -> {
                    setupAlbumsRecyclerView(section as HomeSection<Album>)
                }
                SectionType.RECENTLY_HEARD, SectionType.SUGGESTIONS -> {
                    setupRecentSuggestRecyclerView(section as HomeSection<Song>)
                }
            }
        }

        private fun setupFeaturedSongsRecyclerView(section: HomeSection<Song>) {
            val adapter = FeaturedSongsAdapter { song ->
                onSongClick(song, section.sectionType)
            }
            binding.rvChild.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                this.adapter = adapter
            }
            adapter.submitList(section.items)
        }

        private fun setupArtistsRecyclerView(section: HomeSection<Artist>) {
            val adapter = TopArtistsAdapter(onArtistClick)
            binding.rvChild.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = adapter
            }
            adapter.submitList(section.items)
        }

        private fun setupAlbumsRecyclerView(section: HomeSection<Album>) {
            val adapter = FeaturedAlbumsAdapter(onAlbumClick)
            binding.rvChild.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = adapter
            }
            adapter.submitList(section.items)
        }

        private fun setupRecentSuggestRecyclerView(section: HomeSection<Song>) {
            val adapter = RecentSuggestAdapter { song ->
                onSongClick(song, section.sectionType)
            }
            binding.rvChild.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = adapter
            }
            adapter.submitList(section.items)
        }
    }
}
