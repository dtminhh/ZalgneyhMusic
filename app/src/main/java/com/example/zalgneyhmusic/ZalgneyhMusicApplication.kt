package com.example.zalgneyhmusic

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

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
 *
 * Note: This class must be registered in the AndroidManifest.xml under
 * the <application> tag using `android:name=".ZalgneyhMusicApplication"`.
 */
@HiltAndroidApp
class ZalgneyhMusicApplication : Application()