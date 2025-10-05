package com.example.zalgneyhmusic.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Generic adapter for child fragments with tabs
 * Can be reused by any parent fragment that needs TabLayout + ViewPager2
 */
class ChildFragmentAdapter(
    fragment: Fragment,
    private val fragmentList: List<() -> Fragment>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position].invoke()
    }
}