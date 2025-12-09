package com.example.zalgneyhmusic.ui.fragment.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.Resource
import com.example.zalgneyhmusic.databinding.FragmentLoginBinding
import com.example.zalgneyhmusic.ui.viewmodel.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts
import com.example.zalgneyhmusic.utils.GoogleSignInHelper
import javax.inject.Inject
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.zalgneyhmusic.ui.utils.PASSWORD_LENGTH_REQUIREMENT

/**
 * Fragment responsible for handling user login functionality.
 *
 * Features:
 * - Observes [AuthViewModel] to track authentication state (loading, success, failure).
 * - Provides input validation for email and password.
 * - Navigates to the sign-up screen or main screen depending on user actions and authentication state.
 * - Uses Hilt's [@AndroidEntryPoint] for dependency injection.
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var googleSignInHelper: GoogleSignInHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        checkCurrentUser()
    }

    /**
     * Observes the login flow from [AuthViewModel] and updates the UI
     * based on the [Resource] state: Loading, Success, or Failure.
     */
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    authViewModel.loginFlow.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                binding.buttonConfirmSignIn.isEnabled = false
                            }

                            is Resource.Success -> {
                                binding.buttonConfirmSignIn.isEnabled = true
                                binding.buttonConfirmSignIn.text = getString(R.string.join)
                                Toast.makeText(requireContext(),
                                    getString(R.string.success), Toast.LENGTH_SHORT).show()

                                authViewModel.resetLoginFlow()
                                findNavController().navigate(R.id.mainFragment)
                            }

                            is Resource.Failure -> {
                                binding.buttonConfirmSignIn.isEnabled = true
                                binding.buttonConfirmSignIn.text = getString(R.string.join)
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error, resource.exception.message),
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            null -> {
                                binding.buttonConfirmSignIn.isEnabled = true
                                binding.buttonConfirmSignIn.text = getString(R.string.join)
                            }
                        }
                    }
                }

                launch {
                    authViewModel.googleSignInFlow.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                binding.buttonGoogleSignIn.isEnabled = false
                            }

                            is Resource.Success -> {
                                binding.buttonGoogleSignIn.isEnabled = true
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.stt_google_sign_in_success),
                                    Toast.LENGTH_SHORT
                                ).show()

                                authViewModel.resetGoogleSignInFlow()
                                findNavController().navigate(R.id.mainFragment)
                            }

                            is Resource.Failure -> {
                                binding.buttonGoogleSignIn.isEnabled = true
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.stt_google_sign_in_failed, resource.exception.message),
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            null -> {
                                binding.buttonGoogleSignIn.isEnabled = true
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Starts the Google Sign-In flow.
     *
     * - Disables the Google Sign-In button to prevent multiple clicks.
     * - Uses the helper to perform Google Sign-In.
     * - On success: forwards the `idToken` to ViewModel for Firebase authentication.
     * - On failure:
     *   + If there is no Google account on device → prompts user to add one.
     *   + Otherwise → shows a Toast error message.
     * - On unexpected exception: re-enables the button and shows an error Toast.
     */
    private fun startGoogleSignIn() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.buttonGoogleSignIn.isEnabled = false
                val result = googleSignInHelper.signIn(requireActivity())

                result.fold(
                    onSuccess = { credential ->
                        authViewModel.signInWithGoogle(credential.idToken)
                    },
                    onFailure = { exception ->
                        binding.buttonGoogleSignIn.isEnabled = true

                        if (exception.message == getString(R.string.stt_no_google_account)) {
                            // Automatically prompt user to add a Google account
                            showAddAccountDialog()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.stt_google_sign_in_failed, exception.message),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
            } catch (e: Exception) {
                binding.buttonGoogleSignIn.isEnabled = true
                Toast.makeText(
                    requireContext(),
                    getString(R.string.google_sign_in_error_message, e.message),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Shows a dialog prompting the user to add a Google account
     * in order to use Google Sign-In.
     *
     * - "Add Account" button opens device Settings to add a new Google account.
     * - "Cancel" button dismisses the dialog.
     * - The dialog is non-cancelable to ensure user makes a choice.
     */
    private fun showAddAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.google_account_required))
            .setMessage(getString(R.string.to_use_google_sign_in_please_add_a_google_account_to_your_device))
            .setPositiveButton(getString(R.string.add_account)) { _, _ ->
                openAccountSettings()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                // User declined, do nothing
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Opens device settings to allow the user to add a Google account.
     *
     * - Tries to open the "Add Account" screen directly for Google.
     * - If that fails → falls back to opening "Sync Settings".
     * - As a last fallback → opens the main Settings screen.
     */
    private fun openAccountSettings() {
        try {
            // Open Add Account directly
            val intent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf(getString(R.string.link_to_google_login_page)))
            }
            accountSettingsLauncher.launch(intent)
        } catch (_: Exception) {
            try {
                // Fallback: open general Accounts/Sync settings
                val intent = Intent(Settings.ACTION_SYNC_SETTINGS)
                accountSettingsLauncher.launch(intent)
            } catch (_: Exception) {
                // Final fallback: open main Settings
                val intent = Intent(Settings.ACTION_SETTINGS)
                startActivity(intent)
            }
        }
    }

    /**
     * Launcher to handle result when user returns from Settings after
     * adding an account. Simply shows a Toast reminding the user to
     * retry Google Sign-In.
     */
    private val accountSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        Toast.makeText(
            requireContext(),
            getString(R.string.please_try_google_sign_in_again),
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Sets up click listeners for login, sign-up navigation,
     * and Google sign-in (currently a placeholder).
     */
    private fun setupClickListeners() {
        binding.buttonConfirmSignIn.setOnClickListener {
            val email = binding.editTextUserNameSignIn.text.toString().trim()
            val password = binding.editTextPasswordSignIn.text.toString().trim()
            if (validateInput(email, password)) {
                authViewModel.login(email, password)
            }
        }

        binding.textViewSignUp.setOnClickListener {
            findNavController().navigate(R.id.signUpFragment)
        }

        binding.buttonGoogleSignIn.setOnClickListener {
            startGoogleSignIn()
        }
    }

    /**
     * Validates user input for email and password fields.
     *
     * @param email The email entered by the user.
     * @param password The password entered by the user.
     * @return True if input is valid, false otherwise.
     */
    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.editTextUserNameSignIn.error = getString(R.string.email_is_required)
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextUserNameSignIn.error = getString(R.string.email_is_not_valid)
            return false
        }
        if (password.isEmpty()) {
            binding.editTextPasswordSignIn.error = getString(R.string.password_is_required)
            return false
        }
        if (password.length < PASSWORD_LENGTH_REQUIREMENT) {
            binding.editTextPasswordSignIn.error = getString(R.string.password_length_requirements)
            return false
        }
        return true
    }

    /**
     * Checks if a user is already logged in and navigates
     * directly to the main screen if so.
     */
    private fun checkCurrentUser() {
        if (authViewModel.currentUser != null) {
            // User already logged in, navigate to main screen
            findNavController().navigate(R.id.mainFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}