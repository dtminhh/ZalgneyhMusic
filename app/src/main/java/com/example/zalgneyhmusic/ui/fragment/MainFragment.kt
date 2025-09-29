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
import com.example.zalgneyhmusic.ui.adapter.MainFragmentAdapter
import com.example.zalgneyhmusic.ui.viewmodel.auth.AuthViewModel
import com.example.zalgneyhmusic.ui.fragment.auth.LoginFragment
import dagger.hilt.android.AndroidEntryPoint
import androidx.viewpager2.widget.ViewPager2
import com.example.zalgneyhmusic.ui.UIConstants.Navigation.DEFAULT_NAV_INDEX
import com.example.zalgneyhmusic.ui.UIConstants.ViewPager.USER_INPUT_ENABLED

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
    private val navItems by lazy {
        listOf(
            binding.navHome,
            binding.navSong,
            binding.navAlbums,
            binding.navArtist,
            binding.navPlaylist
        )
    }

    // ViewModel providing access to authentication state
    /** Page titles from string resources instead of hardcoded values */
    private val pageTitles by lazy {
        listOf(
            getString(R.string.home),
            getString(R.string.song),
            getString(R.string.albums),
            getString(R.string.artist),
            getString(R.string.playlist)
        )
    }
    private lateinit var mainViewPagerAdapter: MainFragmentAdapter
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
        setupViewPager()
        setupBottomNavigation()
        selectNavItem(DEFAULT_NAV_INDEX) // Use constant instead of hardcoded 0
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
     * Initializes ViewPager2 with adapter and callbacks.
     */
    private fun setupViewPager() {
        mainViewPagerAdapter = MainFragmentAdapter(this)
        binding.apply {
            vp2MainContent.adapter = mainViewPagerAdapter
            vp2MainContent.isUserInputEnabled =
                USER_INPUT_ENABLED // Use constant

            // Listen for page changes from ViewPager
            vp2MainContent.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    selectNavItem(position)
                    updateTopBarTitle(position)
                }
            })
        }

    }

    /**
     * Sets up bottom navigation click listeners.
     */
    private fun setupBottomNavigation() {
        navItems.forEachIndexed { index, navItem ->
            navItem.setOnClickListener {
                if (currentSelectedIndex != index) {
                    binding.vp2MainContent.currentItem = index
                }
            }
        }
    }

    /**
     * Updates visual state of navigation items.
     */
    private fun selectNavItem(selectedIndex: Int) {
        currentSelectedIndex = selectedIndex
        navItems.forEachIndexed { index, navItem ->
            navItem.isSelected = (index == selectedIndex)
        }
    }

    /**
     * Updates top bar title based on current page.
     */
    private fun updateTopBarTitle(position: Int) {
        if (position < pageTitles.size) {
            binding.txtPageTitle.text = pageTitles[position]
        }
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

    companion object {
        const val DEFAULT_SELECTED_INDEX = 0
    }
}