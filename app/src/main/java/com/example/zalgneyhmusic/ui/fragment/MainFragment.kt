package com.example.zalgneyhmusic.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.zalgneyhmusic.R
import com.example.zalgneyhmusic.data.model.utils.GoogleSignInHelper
import com.example.zalgneyhmusic.databinding.FragmentMainBinding
import com.example.zalgneyhmusic.ui.UIConstants.Navigation.DEFAULT_NAV_INDEX
import com.example.zalgneyhmusic.ui.UIConstants.ViewPager.USER_INPUT_ENABLED
import com.example.zalgneyhmusic.ui.adapter.MainFragmentAdapter
import com.example.zalgneyhmusic.ui.fragment.auth.LoginFragment
import com.example.zalgneyhmusic.ui.viewmodel.TopBarState
import com.example.zalgneyhmusic.ui.viewmodel.TopBarViewModel
import com.example.zalgneyhmusic.ui.viewmodel.auth.AuthViewModel
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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
    private var currentSelectedIndex = DEFAULT_SELECTED_INDEX
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var googleSignInHelper: GoogleSignInHelper

    private lateinit var topBarViewModel: TopBarViewModel

    /**
     * Creates and returns the fragment view.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Sets up UI components and checks user authentication.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize shared ViewModel
        topBarViewModel = ViewModelProvider(requireActivity())[TopBarViewModel::class.java]

        // Check if user is logged in; redirect to LoginFragment if not
        if (authViewModel.currentUser == null) {
            findNavController().navigate(R.id.loginFragment)
        }
        setupViewPager()
        setupBottomNavigation()
        setupTopBarObserver()
        setupMiniPlayer()
        selectNavItem(DEFAULT_NAV_INDEX) // Use constant instead of hardcoded 0
    }

    /**
     * Observes TabLayout state changes from child fragments
     */
    private fun setupTopBarObserver() {
        topBarViewModel.topBarState.observe(viewLifecycleOwner) { state ->
            // Cancel any ongoing animations before starting new ones
            binding.tabLayoutOptions.clearAnimation()

            when (state) {
                is TopBarState.ShowTabLayout -> {
                    showTabLayout(state.tabs, state.setupViewPager)
                }

                is TopBarState.Hidden -> {
                    hideTabLayout()
                }
            }
        }

        // Observe tab selection changes from ViewPager swipe
        topBarViewModel.tabSelection.observe(viewLifecycleOwner) { position ->
            // Update TabLayout selection without triggering callback
            if (binding.tabLayoutOptions.isVisible) {
                val tab = binding.tabLayoutOptions.getTabAt(position)
                if (tab != null && !tab.isSelected) {
                    tab.select()
                }
            }
        }
    }

    /**
     * Shows TabLayout with smooth animation
     */
    private fun showTabLayout(
        tabs: List<String>,
        setupViewPager: () -> Unit,
    ) {
        binding.tabLayoutOptions.apply {
            // Stop any ongoing animations
            clearAnimation()
            removeAllTabs()

            // Add tabs
            tabs.forEach { tabTitle ->
                addTab(newTab().setText(tabTitle))
            }

            // Set up tab selection listener
            clearOnTabSelectedListeners()
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.position?.let { position ->
                        animateTabSelection(position)
                        // Call the callback to update ViewPager2 in child fragment
                        topBarViewModel.onTabClicked(position)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    tab?.view?.apply {
                        animate()
                            .scaleX(Animation.SCALE_NORMAL)
                            .scaleY(Animation.SCALE_NORMAL)
                            .alpha(Animation.ALPHA_FULL)
                            .setDuration(Animation.DURATION_TAB_QUICK)
                            .start()
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    tab?.view?.apply {
                        animate()
                            .scaleX(Animation.SCALE_TAB_RESELECTED)
                            .scaleY(Animation.SCALE_TAB_RESELECTED)
                            .setDuration(Animation.DURATION_TAB_QUICK)
                            .withEndAction {
                                animate()
                                    .scaleX(Animation.SCALE_NORMAL)
                                    .scaleY(Animation.SCALE_NORMAL)
                                    .setDuration(Animation.DURATION_TAB_QUICK)
                                    .start()
                            }
                            .start()
                    }
                }
            })

            // Show animation only if currently hidden
            if (visibility != View.VISIBLE) {
                animateTabLayoutShowAdvanced()
            }
        }

        setupViewPager()
    }

    /**
     * Hides TabLayout with smooth animation
     */
    private fun hideTabLayout() {
        binding.tabLayoutOptions.apply {
            if (isVisible) {
                // Clear any ongoing animations
                clearAnimation()
                animateTabLayoutHide()
            }
        }
    }

    /**
     * Animates TabLayout disappearing with smooth collapse effect
     */
    private fun animateTabLayoutHide() {
        binding.tabLayoutOptions.apply {
            animate()
                .alpha(Animation.ALPHA_TRANSPARENT)
                .scaleY(Animation.SCALE_COLLAPSED)
                .translationY(Animation.TRANSLATION_Y_UP)
                .setDuration(Animation.DURATION_HIDE)
                .setInterpolator(android.view.animation.AccelerateInterpolator())
                .withEndAction {
                    visibility = View.GONE
                    // Reset properties for next animation
                    alpha = Animation.ALPHA_FULL
                    scaleY = Animation.SCALE_NORMAL
                    translationY = Animation.TRANSLATION_Y_NONE

                    // Optional: animate parent container collapse
                    animateTopBarExpansion(false)
                }
                .start()
        }
    }

    /**
     * Animates top bar container expansion/collapse with subtle scale effect
     */
    private fun animateTopBarExpansion(expand: Boolean) {
        binding.lnTopBar.apply {
            val startScale = if (expand) Animation.SCALE_TOPBAR_START else Animation.SCALE_NORMAL
            val endScale = if (expand) Animation.SCALE_NORMAL else Animation.SCALE_TOPBAR_START

            scaleX = startScale
            animate()
                .scaleX(endScale)
                .setDuration(if (expand) Animation.DURATION_SHOW else Animation.DURATION_HIDE)
                .setInterpolator(
                    if (expand)
                        android.view.animation.DecelerateInterpolator()
                    else
                        android.view.animation.AccelerateInterpolator()
                )
                .withEndAction {
                    scaleX = Animation.SCALE_NORMAL // Reset to normal
                }
                .start()
        }
    }

    /**
     * Advanced animation with staggered tab appearance
     */
    private fun animateTabLayoutShowAdvanced() {
        binding.tabLayoutOptions.apply {
            visibility = View.VISIBLE
            alpha = Animation.ALPHA_TRANSPARENT

            // Animate container first
            animate()
                .alpha(Animation.ALPHA_FULL)
                .setDuration(Animation.DURATION_CONTAINER)
                .withEndAction {
                    // Animate each tab with stagger effect
                    animateTabsStaggered()
                }
                .start()
        }
    }

    /**
     * Animates each tab appearing with staggered timing
     */
    private fun animateTabsStaggered() {
        val tabLayout = binding.tabLayoutOptions
        val tabCount = tabLayout.tabCount

        for (i in 0 until tabCount) {
            val tab = tabLayout.getTabAt(i)
            tab?.view?.apply {
                alpha = Animation.ALPHA_TRANSPARENT
                scaleX = Animation.SCALE_TAB_INITIAL
                scaleY = Animation.SCALE_TAB_INITIAL

                animate()
                    .alpha(Animation.ALPHA_FULL)
                    .scaleX(Animation.SCALE_NORMAL)
                    .scaleY(Animation.SCALE_NORMAL)
                    .setDuration(Animation.DURATION_CONTAINER)
                    .setStartDelay((i * Animation.STAGGER_DELAY).toLong())
                    .setInterpolator(android.view.animation.OvershootInterpolator())
                    .start()
            }
        }
    }

    /**
     * Bouncy tab selection animation
     */
    private fun animateTabSelection(selectedPosition: Int) {
        val tabLayout = binding.tabLayoutOptions

        // Animate unselected tabs
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            tab?.view?.apply {
                if (i != selectedPosition) {
                    animate()
                        .scaleX(Animation.SCALE_TAB_UNSELECTED)
                        .scaleY(Animation.SCALE_TAB_UNSELECTED)
                        .alpha(Animation.ALPHA_TAB_UNSELECTED)
                        .setDuration(Animation.DURATION_TAB_SELECT)
                        .start()
                }
            }
        }

        // Animate selected tab
        tabLayout.getTabAt(selectedPosition)?.view?.apply {
            animate()
                .scaleX(Animation.SCALE_TAB_SELECTED)
                .scaleY(Animation.SCALE_TAB_SELECTED)
                .alpha(Animation.ALPHA_FULL)
                .setDuration(Animation.DURATION_TAB_SELECT)
                .setInterpolator(android.view.animation.OvershootInterpolator())
                .withEndAction {
                    // Return to normal size after bounce
                    animate()
                        .scaleX(Animation.SCALE_NORMAL)
                        .scaleY(Animation.SCALE_NORMAL)
                        .setDuration(Animation.DURATION_TAB_QUICK)
                        .start()
                }
                .start()
        }
    }


    /**
     * Logs out user from Firebase and Google Sign-In.
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
        binding.imgSearch.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_searchFragment)
        }

        // Open Account Settings Side Sheet when avatar is clicked
        binding.imgAvatar.setOnClickListener {
            openAccountSettings()
        }
    }

    /**
     * Opens Account & Settings Side Sheet with smooth animation
     */
    private fun openAccountSettings() {
        val sideSheet = com.example.zalgneyhmusic.ui.account.AccountSettingsSideSheet.newInstance()
        sideSheet.show(childFragmentManager, "AccountSettingsSideSheet")
    }

    /**
     * Setup mini player functionality
     */
    private fun setupMiniPlayer() {
        setupMiniPlayerExt(binding)
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
     * Clears binding to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val DEFAULT_SELECTED_INDEX = 0

        /**
         * Animation constants for consistent UI behavior
         */
        object Animation {
            // Scale values
            const val SCALE_NORMAL = 1f
            const val SCALE_COLLAPSED = 0f
            const val SCALE_TOPBAR_START = 0.98f
            const val SCALE_TAB_INITIAL = 0.8f
            const val SCALE_TAB_UNSELECTED = 0.9f
            const val SCALE_TAB_SELECTED = 1.1f
            const val SCALE_TAB_RESELECTED = 1.05f

            // Alpha values
            const val ALPHA_TRANSPARENT = 0f
            const val ALPHA_FULL = 1f
            const val ALPHA_TAB_UNSELECTED = 0.7f

            // Translation values
            const val TRANSLATION_Y_NONE = 0f
            const val TRANSLATION_Y_UP = -50f

            // Duration values (milliseconds)
            const val DURATION_TAB_QUICK = 100L
            const val DURATION_TAB_SELECT = 150L
            const val DURATION_CONTAINER = 200L
            const val DURATION_HIDE = 250L
            const val DURATION_SHOW = 300L

            // Other animation values
            const val STAGGER_DELAY = 50
        }
    }
}