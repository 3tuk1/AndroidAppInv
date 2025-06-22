package com.inv.inventryapp.view.analysis

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

private const val NUM_TABS = 3

class AnalysisFragmentAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ShoppingListFragment()
            1 -> CalendarFragment()
            2 -> HistoryFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}

