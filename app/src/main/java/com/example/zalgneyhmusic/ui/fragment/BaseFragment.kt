package com.example.zalgneyhmusic.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import com.example.zalgneyhmusic.ui.handler.MediaActionHandler
import com.example.zalgneyhmusic.ui.navigation.DetailNavigator
import com.example.zalgneyhmusic.ui.viewmodel.fragment.PlayerViewModel
import javax.inject.Inject

/**
 * BaseFragment:
 * 1. Automatically initialize MediaActionHandler.
 * 2. Child Fragment just needs to call `mediaActionHandler.onSongClick(...)`.
 * 3. Does not contain navigate function (because Extension Function is used).
 */
abstract class BaseFragment : Fragment() {

    // Shared ViewModel
    protected val playerViewModel: PlayerViewModel by activityViewModels()

    @Inject
    lateinit var musicRepository: MusicRepository

    // Handler logic Play/Menu
    protected lateinit var mediaActionHandler: MediaActionHandler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // AUTOMATIC CREATION
        mediaActionHandler = MediaActionHandler(
            context = requireContext(),
            fragmentManager = childFragmentManager,
            playerViewModel = playerViewModel,
            // Get Navigator so Handler can switch screens (when selecting "Go to Artist" from the menu)
            navigator = activity as? DetailNavigator,
            musicRepository = musicRepository,
            scope = viewLifecycleOwner.lifecycleScope
        )
    }
}