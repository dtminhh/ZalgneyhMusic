package com.example.zalgneyhmusic.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.zalgneyhmusic.R
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}