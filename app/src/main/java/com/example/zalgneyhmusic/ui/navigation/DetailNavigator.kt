package com.example.zalgneyhmusic.ui.navigation

import com.example.zalgneyhmusic.data.model.domain.DetailType

interface DetailNavigator {
    fun navigatorToDetailScreen(type: DetailType)
}