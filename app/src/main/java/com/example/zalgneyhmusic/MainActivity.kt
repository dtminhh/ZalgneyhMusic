package com.example.zalgneyhmusic

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.zalgneyhmusic.ui.viewmodel.MainViewModel
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.zalgneyhmusic.data.model.domain.DetailType
import com.example.zalgneyhmusic.ui.fragment.detail.AlbumDetailBottomSheet
import com.example.zalgneyhmusic.ui.fragment.detail.ArtistDetailBottomSheet
import com.example.zalgneyhmusic.ui.fragment.detail.PlaylistDetailBottomSheet
import com.example.zalgneyhmusic.ui.navigation.DetailNavigator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * The main entry point of the application.
 *
 * This activity is annotated with [AndroidEntryPoint] to support Hilt dependency injection.
 *
 * Responsibilities:
 * - Sets up the main layout defined in [R.layout.activity_main].
 * - Enables edge-to-edge UI with [enableEdgeToEdge].
 * - Handles window insets (status bar, navigation bar) to adjust view padding dynamically,
 *   ensuring proper layout rendering across devices with different system UI configurations.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), DetailNavigator {
    private val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("MainActivity", "Activating ViewModel: $mainViewModel")
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun navigatorToDetailScreen(type: DetailType) {
        val bottomSheet: BottomSheetDialogFragment = when (type) {
            is DetailType.Artist -> {
                ArtistDetailBottomSheet.newInstance(type.id)
            }
            is DetailType.Album -> {
                AlbumDetailBottomSheet.newInstance(type.id)
            }
            is DetailType.Playlist -> {
                PlaylistDetailBottomSheet.newInstance(type.id)
            }
        }
        bottomSheet.show(supportFragmentManager, "DETAIL_SHEET_TAG")
    }
}