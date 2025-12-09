package com.example.zalgneyhmusic.ui.fragment.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.Resource
import com.example.zalgneyhmusic.databinding.FragmentSignUpBinding
import com.example.zalgneyhmusic.ui.viewmodel.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment responsible for handling user sign-up functionality.
 *
 * Features:
 * - Observes [AuthViewModel] to track sign-up state (loading, success, failure).
 * - Provides input validation for email, password, and confirm password fields.
 * - Navigates to the main screen upon successful registration.
 * - Uses Hilt's [@AndroidEntryPoint] for dependency injection.
 */
@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    // ViewModel injected using Hilt and lifecycle-aware delegation.
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    /**
     * Observes the sign-up flow from [AuthViewModel] and updates the UI
     * based on the [Resource] state: Loading, Success, or Failure.
     */
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.signupFlow.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.buttonConfirmSignUp.isEnabled = false
                    }

                    is Resource.Success -> {
                        binding.buttonConfirmSignUp.isEnabled = true
                        binding.buttonConfirmSignUp.text = getString(R.string.confirm)
                        Toast.makeText(requireContext(), getString(R.string.toast_sign_up_success), Toast.LENGTH_SHORT)
                            .show()
                        // Navigate to main screen
                        findNavController().navigate(R.id.mainFragment)
                    }

                    is Resource.Failure -> {
                        binding.buttonConfirmSignUp.isEnabled = true
                        binding.buttonConfirmSignUp.text = getString(R.string.confirm)
                        Toast.makeText(
                            requireContext(),
                            "Error: ${resource.exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    null -> {
                        binding.buttonConfirmSignUp.isEnabled = true
                        binding.buttonConfirmSignUp.text = getString(R.string.confirm)
                    }
                }
            }
        }
    }

    /**
     * Sets up click listeners for the sign-up button and navigation back to login.
     */
    private fun setupClickListeners() {
        binding.buttonConfirmSignUp.setOnClickListener {
            val email = binding.editTextUserNameSignUp.text.toString().trim()
            val password = binding.editTextPasswordSignUp.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPasswordSignUp.text.toString().trim()
            if (validateInput(email, password, confirmPassword)) {
                authViewModel.signup(email, password)
            }
        }
        binding.textViewSignUp.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Validates user input for email, password, and confirm password fields.
     *
     * @param email The email entered by the user.
     * @param password The password entered by the user.
     * @param confirmPassword The repeated password for confirmation.
     * @return True if input is valid, false otherwise.
     */
    private fun validateInput(email: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty()) {
            binding.editTextUserNameSignUp.error = "Email is required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextUserNameSignUp.error = "Email is not valid"
            return false
        }
        if (password.isEmpty()) {
            binding.editTextPasswordSignUp.error = "Password is required"
            return false
        }
        if (password.length < 6) {
            binding.editTextPasswordSignUp.error = "Password must be at least 6 characters"
            return false
        }
        if (confirmPassword.isEmpty()) {
            binding.editTextConfirmPasswordSignUp.error = "Confirm password is required"
            return false
        }
        if (password != confirmPassword) {
            binding.editTextConfirmPasswordSignUp.error = "Passwords do not match"
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}