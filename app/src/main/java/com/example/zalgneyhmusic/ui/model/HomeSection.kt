package com.example.zalgneyhmusic.ui.model

/**
 * Enum defining section types in Home Screen
 * Used to identify and render different content sections
 */
enum class SectionType {
    /** Featured songs section (top 5 trending songs) */
    FEATURED_SONGS,

    /** Top artists section (circular avatar grid) */
    TOP_ARTISTS,

    /** Featured albums section (album cover grid) */
    FEATURED_ALBUMS,

    /** Recently played songs section */
    RECENTLY_HEARD,

    /** Personalized song suggestions section */
    SUGGESTIONS
}

/**
 * Data model representing a section in Home Screen
 * Generic type [T] can be Song, Artist, or Album depending on section type
 *
 * @param T The type of items in this section (Song, Artist, or Album)
 * @property title Display title for the section header
 * @property sectionType Type of section that determines the layout and adapter
 * @property items List of items to display in this section
 */
data class HomeSection<T>(
    val title: String,
    val sectionType: SectionType,
    val items: List<T>
)
