package com.example.zalgneyhmusic

import android.app.Application
import com.example.zalgneyhmusic.data.local.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Custom [Application] class for the Zalgneyh Music app.
 *
 * This class is annotated with [HiltAndroidApp] to initialize
 * Hilt's dependency injection framework at the application level.
 *
 * Responsibilities:
 * - Serves as the entry point for dependency injection across the app.
 * - Ensures that all Hilt components (e.g., SingletonComponent) are properly
 *   initialized before any other Android components (Activities, Fragments, etc.)
 *   are created.
 * - Initializes local database with sample data on first launch
 *
 * Note: This class must be registered in the AndroidManifest.xml under
 * the <application> tag using `android:name=".ZalgneyhMusicApplication"`.
 */
@HiltAndroidApp
class ZalgneyhMusicApplication : Application() {

    @Inject
    lateinit var databaseInitializer: DatabaseInitializer

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Initialize database with sample data on first launch
        applicationScope.launch {
            databaseInitializer.initializeDatabase()
        }
    }
}