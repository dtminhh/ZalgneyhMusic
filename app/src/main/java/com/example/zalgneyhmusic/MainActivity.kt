package com.example.zalgneyhmusic

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}