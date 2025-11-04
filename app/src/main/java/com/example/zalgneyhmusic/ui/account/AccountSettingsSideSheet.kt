package com.example.zalgneyhmusic.ui.account

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.databinding.SideSheetAccountSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

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
            Glide.with(this@AccountSettingsSideSheet)
                .load(viewModel.getUserPhotoUrl())
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)
                .centerCrop()
                .into(imgUserAvatar)

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
            AccountSettingsItem.Action(AccountSettingsAction.ViewStats),
            AccountSettingsItem.Action(AccountSettingsAction.ManageSubscription),

            // App Settings Section
            AccountSettingsItem.Section(R.string.section_app_settings),
            AccountSettingsItem.Action(AccountSettingsAction.Theme),
            AccountSettingsItem.Action(AccountSettingsAction.AudioQuality),
            AccountSettingsItem.Action(AccountSettingsAction.Notifications),
            AccountSettingsItem.Action(AccountSettingsAction.Storage),
            AccountSettingsItem.Action(AccountSettingsAction.Language),

            // Help & About Section
            AccountSettingsItem.Section(R.string.section_help_about),
            AccountSettingsItem.Action(AccountSettingsAction.Help),
            AccountSettingsItem.Action(AccountSettingsAction.About),
            AccountSettingsItem.Action(AccountSettingsAction.PrivacyPolicy),

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
            AccountSettingsAction.ViewStats -> {
                Toast.makeText(context, "View Statistics - Coming soon", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            AccountSettingsAction.ManageSubscription -> {
                Toast.makeText(context, "Manage Subscription - Coming soon", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            AccountSettingsAction.Theme -> {
                Toast.makeText(context, "Theme Settings - Coming soon", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            AccountSettingsAction.AudioQuality -> {
                Toast.makeText(context, "Audio Quality - Coming soon", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            AccountSettingsAction.Notifications -> {
                Toast.makeText(context, "Notification Settings - Coming soon", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            AccountSettingsAction.Storage -> {
                Toast.makeText(context, "Storage Settings - Coming soon", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            AccountSettingsAction.Language -> {
                Toast.makeText(context, "Language Settings - Coming soon", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            AccountSettingsAction.Help -> {
                Toast.makeText(context, "Help & Support - Coming soon", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            AccountSettingsAction.About -> {
                Toast.makeText(context, "About - Coming soon", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            AccountSettingsAction.PrivacyPolicy -> {
                Toast.makeText(context, "Privacy Policy - Coming soon", Toast.LENGTH_SHORT).show()
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
        const val TAG = "AccountSettingsSideSheet"

        fun newInstance() = AccountSettingsSideSheet()
    }
}

