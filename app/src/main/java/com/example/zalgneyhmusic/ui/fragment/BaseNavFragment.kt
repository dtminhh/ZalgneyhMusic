package com.example.zalgneyhmusic.ui.fragment

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.zalgneyhmusic.ui.adapter.ChildFragmentAdapter
import com.example.zalgneyhmusic.ui.viewmodel.TopBarViewModel

/**
 * Base class for navigation fragments with TabLayout and child ViewPager2 support
 */
abstract class BaseNavFragment : Fragment() {

    protected lateinit var topBarViewModel: TopBarViewModel
    protected lateinit var childAdapter: ChildFragmentAdapter
    protected var childViewPager: ViewPager2? = null
    private var isTabLayoutSetup = false

    // Add missing properties
    private var currentTabs: List<String>? = null
    private var viewPagerCallback: ViewPager2.OnPageChangeCallback? = null

    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBarViewModel = ViewModelProvider(requireActivity())[TopBarViewModel::class.java]
    }

    /**
     * Setup TabLayout and ViewPager2 with tabs and fragments
     */
    protected fun setupTabsAndFragments(
        viewPager: ViewPager2,
        tabs: List<String>,
        fragments: List<() -> Fragment>
    ) {
        childViewPager = viewPager
        currentTabs = tabs


        // Create adapter with fragments
        childAdapter = ChildFragmentAdapter(this, fragments)
        viewPager.adapter = childAdapter
        viewPager.isUserInputEnabled = true

        // Create new callback
        viewPagerCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Update TabLayout selection when user swipes ViewPager
                topBarViewModel.updateTabSelection(position)
            }
        }

        // Register the callback
        viewPagerCallback?.let { viewPager.registerOnPageChangeCallback(it) }

        // Show TabLayout in parent only if this fragment is currently visible
        if (isResumed && !isTabLayoutSetup) {
            showTabLayoutWithViewPager(tabs) { position ->
                // This callback is called when user clicks TabLayout
                viewPager.setCurrentItem(position, true) // true for smooth scrolling
            }
            isTabLayoutSetup = true
        }
    }


    /**
     * Shows TabLayout with ViewPager2 setup
     */
    private fun showTabLayoutWithViewPager(
        tabs: List<String>,
        onTabSelected: (Int) -> Unit
    ) {
        topBarViewModel.showTabLayout(
            tabs = tabs,
            setupViewPager = {
                // ViewPager already setup in setupTabsAndFragments
            },
            onTabSelected = onTabSelected
        )
    }

    /**
     * Hides TabLayout when fragment is not visible
     */
    protected fun hideTabLayout() {
        topBarViewModel.hideTabLayout()
        isTabLayoutSetup = false
    }

    override fun onResume() {
        super.onResume()
        // Only setup if ViewPager is already initialized but TabLayout is not setup
        if (childViewPager != null && !isTabLayoutSetup) {
            // Delay slightly to avoid conflicts with other fragments
            view?.postDelayed({
                if (isResumed) {
                    setupTabLayoutAndViewPager()
                }
            }, 100)
        }
    }

    override fun onPause() {
        super.onPause()
        hideTabLayout()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isTabLayoutSetup = false
        if (childViewPager != null && viewPagerCallback != null) {
            childViewPager?.unregisterOnPageChangeCallback(viewPagerCallback!!)
            viewPagerCallback = null
        }
    }

    /**
     * Abstract method for child fragments to implement their specific setup
     */
    protected abstract fun setupTabLayoutAndViewPager()
}
