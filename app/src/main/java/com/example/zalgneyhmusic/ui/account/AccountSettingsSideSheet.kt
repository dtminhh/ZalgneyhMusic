package com.example.zalgneyhmusic.ui.account

import ImageUtils
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.databinding.SideSheetAccountSettingsBinding
import com.example.zalgneyhmusic.ui.utils.StorageHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Beautiful Material Design Side Sheet for Account & Settings
 * Slides from LEFT to RIGHT
 * Following MVVM pattern and Clean Architecture principles
 * Consistent with app's existing MoreOptions design
 */
@AndroidEntryPoint
class AccountSettingsSideSheet : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.SideSheetDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.let { window ->
            window.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            window.setGravity(Gravity.START) // Align to left side
            window.attributes?.windowAnimations = R.style.SideSheetAnimation
        }
        return dialog
    }

    private var _binding: SideSheetAccountSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountSettingsViewModel by viewModels()
    private lateinit var adapter: AccountSettingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SideSheetAccountSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.apply {
            // Load user info
            txtUserName.text = viewModel.getUserDisplayName()
            txtUserEmail.text = viewModel.getUserEmail()

            // Load avatar
            ImageUtils.loadImageRounded(imgUserAvatar, viewModel.getUserPhotoUrl())

            // Setup RecyclerView
            adapter = AccountSettingsAdapter { action ->
                handleAction(action)
            }

            rvAccountSettings.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = this@AccountSettingsSideSheet.adapter
            }

            // Submit settings list
            adapter.submitList(getSettingsItems())
        }
    }

    private fun setupObservers() {
        viewModel.logoutSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                dismiss()
                findNavController().navigate(R.id.loginFragment)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Logout failed. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getSettingsItems(): List<AccountSettingsItem> {
        return listOf(
            // Account Section
            AccountSettingsItem.Section(R.string.section_account),
            AccountSettingsItem.Action(AccountSettingsAction.EditProfile),
            AccountSettingsItem.Action(AccountSettingsAction.ManageSubscription),

            // App Settings Section
            AccountSettingsItem.Section(R.string.section_app_settings),
            AccountSettingsItem.Action(AccountSettingsAction.Storage),
            AccountSettingsItem.Action(AccountSettingsAction.Language),

            // Help & About Section
            AccountSettingsItem.Section(R.string.section_help_about),
            AccountSettingsItem.Action(AccountSettingsAction.About),

            // Danger Zone
            AccountSettingsItem.Section(R.string.section_danger_zone),
            AccountSettingsItem.Action(AccountSettingsAction.Logout)
        )
    }

    private fun handleAction(action: AccountSettingsAction) {
        when (action) {
            AccountSettingsAction.EditProfile -> {
                Toast.makeText(context, "Edit Profile - Coming soon", Toast.LENGTH_SHORT).show()
                dismiss()
            }

            AccountSettingsAction.ManageSubscription -> {
                Toast.makeText(context, "Manage Subscription - Coming soon", Toast.LENGTH_SHORT)
                    .show()
                dismiss()
            }

            AccountSettingsAction.Storage -> {
                showStorageDialog()
            }

            AccountSettingsAction.Language -> {
                showLanguageSelectionDialog()
            }

            AccountSettingsAction.About -> {
                Toast.makeText(context, "About - Coming soon", Toast.LENGTH_SHORT).show()
                dismiss()
            }

            AccountSettingsAction.Logout -> {
                showLogoutConfirmation()
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.logout_confirmation_title)
            .setMessage(R.string.logout_confirmation_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Displays a dialog for selecting the application language.
     *
     * Supports switching between English ("en") and Vietnamese ("vi").
     * Uses [AppCompatDelegate.setApplicationLocales] to persist the choice
     * and automatically recreate the Activity to apply the new locale.
     */
    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("English", "Tiếng Việt")
        val languageCodes = arrayOf("en", "vi")

        // Determine current selection index
        val currentLocale = AppCompatDelegate.getApplicationLocales().getFirstMatch(languageCodes)
        val checkedItem = if (currentLocale?.language == "vi") 1 else 0

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.settings_language))
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                val selectedLang = languageCodes[which]

                // Apply new locale
                val appLocale = LocaleListCompat.forLanguageTags(selectedLang)
                AppCompatDelegate.setApplicationLocales(appLocale)

                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Displays a dialog for managing application storage.
     *
     * Shows the current total cache size with a visual breakdown and progress indicator.
     * Allows the user to clear the application cache (including Glide and system cache)
     * asynchronously with immediate UI feedback.
     */
    private fun showStorageDialog() {
        val builder = AlertDialog.Builder(requireContext())
        // Inflate custom layout
        val dialogBinding =
            com.example.zalgneyhmusic.databinding.DialogStorageManagerBinding.inflate(layoutInflater)
        builder.setView(dialogBinding.root)

        val dialog = builder.create()
        // Transparent background for rounded corners
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        dialogBinding.apply {
            // [1] Calculate storage data
            val totalSize = StorageHelper.getCacheSize(requireContext())
            // Simulate breakdown (Glide usually takes 60-80%)
            val imageSize = (totalSize * 0.7).toLong()
            val otherSize = totalSize - imageSize

            // [2] Update UI
            txtTotalSize.text = StorageHelper.formatSize(requireContext(), totalSize)
            txtImageCache.text = StorageHelper.formatSize(requireContext(), imageSize)
            txtOtherCache.text = StorageHelper.formatSize(requireContext(), otherSize)

            progressStorage.progress =
                ((totalSize.toFloat() / (500 * 1024 * 1024)) * 100).toInt().coerceIn(5, 100)

            // Progress bar visual (using 500MB as an arbitrary cap for 100%)
            btnClear.setOnClickListener {
                btnClear.text = getString(R.string.cleaning)
                btnClear.isEnabled = false

                lifecycleScope.launch {
                    // Clear cache on IO thread
                    StorageHelper.clearAppCache(requireContext())

                    // Calculate storage data
                    val newTotalSize = StorageHelper.getCacheSize(requireContext())
                    val newImageSize = (newTotalSize * 0.7).toLong()
                    val newOtherSize = newTotalSize - newImageSize

                    // Update UI post-cleanup
                    txtTotalSize.text = StorageHelper.formatSize(requireContext(), newTotalSize)
                    txtImageCache.text = StorageHelper.formatSize(requireContext(), newImageSize)
                    txtOtherCache.text = StorageHelper.formatSize(requireContext(), newOtherSize)

                    val newProgress = ((newTotalSize.toFloat() / (500 * 1024 * 1024)) * 100).toInt()
                        .coerceIn(0, 100)
                    progressStorage.setProgressCompat(newProgress, true)

                    // [3] Handle "Clean" action
                    btnClear.text = getString(R.string.cleaned)
                }
            }
            btnCancel.setOnClickListener { dialog.dismiss() }
        }
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val params = window.attributes
            params.width = resources.getDimensionPixelSize(R.dimen.side_sheet_width)
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            params.gravity = Gravity.START // Align to left
            window.attributes = params

            // Make dialog full height and aligned to left
            window.setLayout(
                resources.getDimensionPixelSize(R.dimen.side_sheet_width),
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AccountSettingsSideSheet()
    }
}

