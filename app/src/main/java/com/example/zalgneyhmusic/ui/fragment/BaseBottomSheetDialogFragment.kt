package com.example.zalgneyhmusic.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.zalgneyhmusic.ui.handler.MediaActionHandler
import com.example.zalgneyhmusic.ui.navigation.DetailNavigator
import com.example.zalgneyhmusic.ui.viewmodel.fragment.PlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {
    protected val playerViewModel: PlayerViewModel by activityViewModels()
    protected lateinit var mediaActionHandler: MediaActionHandler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaActionHandler = MediaActionHandler(
            requireContext(),
            childFragmentManager,
            playerViewModel,
            activity as? DetailNavigator
        )
    }
}