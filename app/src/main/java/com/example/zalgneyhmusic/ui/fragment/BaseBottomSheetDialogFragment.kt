package com.example.zalgneyhmusic.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import com.example.zalgneyhmusic.ui.handler.MediaActionHandler
import com.example.zalgneyhmusic.ui.navigation.DetailNavigator
import com.example.zalgneyhmusic.ui.viewmodel.fragment.PlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {
    protected val playerViewModel: PlayerViewModel by activityViewModels()
    protected lateinit var mediaActionHandler: MediaActionHandler

    @Inject
    lateinit var musicRepository: MusicRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaActionHandler = MediaActionHandler(
            requireContext(),
            childFragmentManager,
            playerViewModel,
            activity as? DetailNavigator,
            musicRepository = musicRepository,
            scope = viewLifecycleOwner.lifecycleScope
        )
    }
}