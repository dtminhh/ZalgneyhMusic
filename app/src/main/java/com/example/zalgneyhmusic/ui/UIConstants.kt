package com.example.zalgneyhmusic.ui

/** Password length requirement for login/sign up type box */
const val PASSWORD_LENGTH_REQUIREMENT = 6
/**
 * App-wide UI constants to avoid hardcoded values throughout the application.
 *
 * This object contains nested objects organizing constants by feature area,
 * making them easy to find and maintain.
 */
object UIConstants {

    /**
     * Navigation related constants for bottom navigation and ViewPager.
     */
    object Navigation {
        /** Default navigation item index pointing to Home tab */
        const val DEFAULT_NAV_INDEX = 0
    }

    /**
     * ViewPager2 configuration constants.
     */
    object ViewPager {
        /** Disable user swipe input to control navigation programmatically */
        const val USER_INPUT_ENABLED = false
    }
}