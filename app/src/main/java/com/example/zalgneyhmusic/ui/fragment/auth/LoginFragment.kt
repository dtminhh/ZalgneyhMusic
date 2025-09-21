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
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.databinding.FragmentLoginBinding
import com.example.zalgneyhmusic.ui.fragment.BaseFragment
import com.example.zalgneyhmusic.ui.viewmodel.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
class LoginFragment : BaseFragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

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
            authViewModel.loginFlow.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.buttonConfirmSignIn.isEnabled = false
                    }

                    is Resource.Success -> {
                        binding.buttonConfirmSignIn.isEnabled = true
                        binding.buttonConfirmSignIn.text = getString(R.string.join)
                        Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                        // Navigate to main screen
                        findNavController().navigate(R.id.mainFragment)
                    }

                    is Resource.Failure -> {
                        binding.buttonConfirmSignIn.isEnabled = true
                        binding.buttonConfirmSignIn.text = getString(R.string.join)
                        Toast.makeText(
                            requireContext(),
                            "Error: ${resource.exception.message}",
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
            // TODO: Implement Google Sign In
            Toast.makeText(requireContext(), "Google Sign In coming soon!", Toast.LENGTH_SHORT)
                .show()
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
            binding.editTextUserNameSignIn.error = "Email is required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextUserNameSignIn.error = "Email is not valid"
            return false
        }
        if (password.isEmpty()) {
            binding.editTextPasswordSignIn.error = "Password is required"
            return false
        }
        if (password.length < 6) {
            binding.editTextPasswordSignIn.error = "Password must be at least 6 characters"
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