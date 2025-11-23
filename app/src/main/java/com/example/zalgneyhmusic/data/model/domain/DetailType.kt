package com.example.zalgneyhmusic.data.model.domain

sealed class DetailType {
    data class Album(val id: String) : DetailType()
    data class Artist(val id: String) : DetailType()
    data class Playlist(val id: String) : DetailType()
}