package com.example.zalgneyhmusic.ui.viewmodel

import androidx.lifecycle.ViewModel

/**
 * Base class for all ViewModels in the application.
 *
 * Extends [ViewModel] and can be used to share common functionality
 * across multiple ViewModels in the project.
 *
 * Benefits:
 * - Provides a central place to add common logic (e.g., error handling,
 *   coroutine exception handling, or shared utilities).
 * - Helps maintain consistency and reduces boilerplate code when scaling
 *   the application with multiple ViewModels.
 *
 * Currently, this class does not add extra behavior beyond [ViewModel],
 * but serves as a foundation for future enhancements.
 */
open class BaseViewModel : ViewModel()