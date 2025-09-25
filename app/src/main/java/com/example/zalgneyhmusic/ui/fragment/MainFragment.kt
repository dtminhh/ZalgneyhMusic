package com.example.zalgneyhmusic.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.utils.GoogleSignInHelper
import javax.inject.Inject
import com.example.zalgneyhmusic.databinding.FragmentMainBinding
import com.example.zalgneyhmusic.ui.viewmodel.auth.AuthViewModel
import com.example.zalgneyhmusic.ui.fragment.auth.LoginFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main fragment displayed after successful login or sign-up.
 *
 * Responsibilities:
 * - Acts as the entry point for the authenticated part of the app.
 * - Verifies whether the user is currently logged in via [AuthViewModel].
 * - If no user is logged in, redirects back to the [LoginFragment].
 *
 * This fragment is annotated with [@AndroidEntryPoint] to allow
 * Hilt dependency injection for [AuthViewModel].
 */
@AndroidEntryPoint
class MainFragment : BaseFragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    // ViewModel providing access to authentication state
    private val authViewModel: AuthViewModel by viewModels()
    @Inject
    lateinit var googleSignInHelper: GoogleSignInHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Check if user is logged in; redirect to LoginFragment if not
        if (authViewModel.currentUser == null) {
            findNavController().navigate(R.id.loginFragment)
        }
    }

    /**
     * Logs the user out of the application.
     *
     * - Calls ViewModel to perform Firebase logout.
     * - Signs out the user from Google Sign-In.
     * - Navigates back to the Login screen.
     */
    private fun logout() {
        // Firebase logout
        authViewModel.logout()
        // Google logout
        googleSignInHelper.signOut()
        // Navigate to login
        findNavController().navigate(R.id.loginFragment)
    }

    /**
     * Called when the fragment's view is being destroyed.
     *
     * - Clears the binding reference to avoid memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}