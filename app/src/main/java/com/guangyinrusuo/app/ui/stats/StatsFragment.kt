/*
 * 光阴如梭 - 个人效率工具
 * StatsFragment.kt
 *
 * 统计页面Fragment
 * 使用 TabLayout + ViewPager2 实现「专注记录」和「APP使用」两个Tab
 * 专注记录Tab显示日/周柱状图
 * APP使用Tab显示饼图
 */

package com.guangyinrusuo.app.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.guangyinrusuo.app.R
import com.guangyinrusuo.app.databinding.FragmentStatsBinding

/**
 * 统计Fragment
 * 承载两个子页面的ViewPager
 */
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: StatsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[StatsViewModel::class.java]

        // 初始化ViewPager
        setupViewPager()

        // 加载数据
        viewModel.loadAllStats()
    }

    /**
     * 配置 ViewPager2 + TabLayout
     * 两个子页面：FocusStatsPage / AppUsageStatsPage
     */
    private fun setupViewPager() {
        // 创建ViewPager适配器 — 包含两个子Fragment
        val adapter = StatsPagerAdapter(this)
        binding.viewPagerStats.adapter = adapter

        // 将TabLayout与ViewPager绑定
        TabLayoutMediator(binding.tabStats, binding.viewPagerStats) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.stats_focus_record)
                1 -> getString(R.string.stats_app_usage)
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
