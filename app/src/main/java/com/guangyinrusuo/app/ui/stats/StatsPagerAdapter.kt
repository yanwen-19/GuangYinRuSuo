/*
 * 光阴如梭 - 个人效率工具
 * StatsPagerAdapter.kt
 *
 * ViewPager2 适配器 — 承载两个子页面：
 * 0: FocusStatsPage (专注记录 — 柱状图)
 * 1: AppUsageStatsPage (APP使用 — 饼图)
 */

package com.guangyinrusuo.app.ui.stats

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * 统计ViewPager适配器
 * 使用 FragmentStateAdapter (ViewPager2 推荐方式)
 */
class StatsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FocusStatsFragment()   // 专注记录
            1 -> AppUsageStatsFragment() // APP使用
            else -> FocusStatsFragment()
        }
    }
}
