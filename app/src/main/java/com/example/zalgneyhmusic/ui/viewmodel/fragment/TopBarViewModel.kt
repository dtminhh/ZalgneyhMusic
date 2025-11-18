package com.example.zalgneyhmusic.ui.viewmodel.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.zalgneyhmusic.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopBarViewModel @Inject constructor() : BaseViewModel() {

    private val _topBarState = MutableLiveData<TopBarState>(TopBarState.Hidden)
    val topBarState: LiveData<TopBarState> = _topBarState

    private val _tabSelection = MutableLiveData<Int>(0)
    val tabSelection: LiveData<Int> = _tabSelection

    private var hideJob: Job? = null
    private var currentOnTabSelected: ((Int) -> Unit)? = null

    fun showTabLayout(
        tabs: List<String>,
        setupViewPager: () -> Unit,
        onTabSelected: (Int) -> Unit
    ) {
        // Cancel any pending hide operation
        hideJob?.cancel()

        // Store the callback for later use
        currentOnTabSelected = onTabSelected

        _topBarState.value = TopBarState.ShowTabLayout(
            tabs = tabs,
            setupViewPager = setupViewPager,
            onTabSelected = onTabSelected
        )
    }

    fun hideTabLayout() {
        // Add small delay to prevent flicker when quickly switching between nav fragments
        hideJob?.cancel()
        hideJob = CoroutineScope(Dispatchers.Main).launch {
            delay(150) // Small delay to prevent flicker
            _topBarState.value = TopBarState.Hidden
            _tabSelection.value = 0 // Reset selection
            currentOnTabSelected = null
        }
    }

    /**
     * Updates tab selection from ViewPager swipe
     */
    fun updateTabSelection(position: Int) {
        _tabSelection.value = position
    }

    /**
     * Called when user clicks on a tab in TabLayout
     */
    fun onTabClicked(position: Int) {
        currentOnTabSelected?.invoke(position)
        _tabSelection.value = position
    }

    override fun onCleared() {
        super.onCleared()
        hideJob?.cancel()
        currentOnTabSelected = null
    }
}

/**
 * Represents different states of the top bar
 */
sealed class TopBarState {
    object Hidden : TopBarState()
    data class ShowTabLayout(
        val tabs: List<String>,
        val setupViewPager: () -> Unit,
        val onTabSelected: (Int) -> Unit
    ) : TopBarState()
}
