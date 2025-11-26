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
    object ManageSubscription : AccountSettingsAction(
        R.string.account_manage_subscription,
        R.drawable.ic_playlist_placeholder
    )

    object Storage : AccountSettingsAction(R.string.settings_storage, R.drawable.ic_album)
    object Language :
        AccountSettingsAction(R.string.settings_language, R.drawable.ic_settings_language)

    // About
    object About : AccountSettingsAction(R.string.account_about, R.drawable.ic_info)

    // Danger Zone
    object Logout : AccountSettingsAction(R.string.account_logout, R.drawable.ic_logout, true)
}

