package com.guangyinrusuo.app.ui.stats
import androidx.fragment.app.Fragment
class StatsPagerAdapter(f: Fragment) : androidx.viewpager2.adapter.FragmentStateAdapter(f) {
    override fun getItemCount() = 0
    override fun createFragment(p: Int) = Fragment()
}
