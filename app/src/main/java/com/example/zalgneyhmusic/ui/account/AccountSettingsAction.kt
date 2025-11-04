package com.example.zalgneyhmusic.ui.account

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.zalgneyhmusic.R

/**
 * Type-safe actions for Account & Settings Side Sheet
 * Following MVVM pattern and consistent with app architecture
 */
sealed class AccountSettingsAction(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    val isDanger: Boolean = false
) {
    // Account Actions
    object EditProfile : AccountSettingsAction(R.string.account_edit_profile, R.drawable.ic_person)
    object ViewStats : AccountSettingsAction(R.string.account_view_stats, R.drawable.ic_album)
    object ManageSubscription : AccountSettingsAction(R.string.account_manage_subscription, R.drawable.ic_playlist_placeholder)

    // App Settings Actions
    object Theme : AccountSettingsAction(R.string.settings_theme, R.drawable.ic_settings_theme)
    object AudioQuality : AccountSettingsAction(R.string.settings_audio_quality, R.drawable.ic_music_note)
    object Notifications : AccountSettingsAction(R.string.settings_notifications, R.drawable.notifications)
    object Storage : AccountSettingsAction(R.string.settings_storage, R.drawable.ic_album)
    object Language : AccountSettingsAction(R.string.settings_language, R.drawable.ic_settings_language)

    // Help & About
    object Help : AccountSettingsAction(R.string.account_help, R.drawable.ic_help)
    object About : AccountSettingsAction(R.string.account_about, R.drawable.ic_info)
    object PrivacyPolicy : AccountSettingsAction(R.string.account_privacy_policy, R.drawable.ic_privacy)

    // Danger Zone
    object Logout : AccountSettingsAction(R.string.account_logout, R.drawable.ic_logout, true)
}

/**
 * Grouped sections for better UX
 */
data class AccountSettingsSection(
    @StringRes val titleRes: Int,
    val actions: List<AccountSettingsAction>
)

