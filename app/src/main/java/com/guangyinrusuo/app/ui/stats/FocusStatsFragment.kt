/*
 * 光阴如梭 - 个人效率工具
 * FocusStatsFragment.kt
 *
 * 专注记录统计子页面
 * 使用 MPAndroidChart 的 BarChart 显示本周每日专注时长
 * 顶部显示今日专注总时长
 */

package com.guangyinrusuo.app.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.guangyinrusuo.app.R
import com.guangyinrusuo.app.databinding.FragmentFocusStatsBinding

/**
 * 专注记录统计Fragment
 * 柱状图展示周数据
 */
class FocusStatsFragment : Fragment() {

    private var _binding: FragmentFocusStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: StatsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFocusStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 获取父Fragment的ViewModel (共享同一实例)
        viewModel = ViewModelProvider(requireParentFragment())[StatsViewModel::class.java]

        // 观察周数据
        viewModel.weeklyFocusData.observe(viewLifecycleOwner) { data ->
            updateBarChart(data)
        }

        // 观察今日数据
        viewModel.todayFocusSeconds.observe(viewLifecycleOwner) { seconds ->
            binding.tvTodayTotal.text = viewModel.formatFocusSeconds(seconds)
        }
    }

    /**
     * 更新柱状图
     */
    private fun updateBarChart(data: List<Pair<String, Int>>) {
        val entries = data.mapIndexed { index, pair ->
            BarEntry(index.toFloat(), pair.second / 60f) // 转换为分钟显示
        }

        val labels = data.map { it.first }

        val dataSet = BarDataSet(entries, "专注时长(分钟)").apply {
            color = resources.getColor(R.color.pomo_focus, null)
            valueTextSize = 10f
            setDrawValues(true)
        }

        binding.barChart.apply {
            this.data = BarData(dataSet)
            description.isEnabled = false
            setFitBars(true)
            animateY(500)

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }

            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
            }
            axisRight.isEnabled = false

            legend.isEnabled = true
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
