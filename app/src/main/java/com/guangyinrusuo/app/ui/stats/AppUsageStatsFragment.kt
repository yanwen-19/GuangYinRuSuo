/*
 * 光阴如梭 - 个人效率工具
 * AppUsageStatsFragment.kt
 *
 * APP使用统计子页面
 * 使用 MPAndroidChart 的 PieChart 显示各应用使用时长占比
 */

package com.guangyinrusuo.app.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.guangyinrusuo.app.R
import com.guangyinrusuo.app.databinding.FragmentAppUsageStatsBinding

/**
 * APP使用统计Fragment
 * 饼图显示各应用时间占比
 */
class AppUsageStatsFragment : Fragment() {

    private var _binding: FragmentAppUsageStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: StatsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppUsageStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireParentFragment())[StatsViewModel::class.java]

        viewModel.appUsageList.observe(viewLifecycleOwner) { items ->
            updatePieChart(items)
            updateUsageList(items)
        }
    }

    /**
     * 更新饼图
     */
    private fun updatePieChart(items: List<StatsViewModel.AppUsageItem>) {
        if (items.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.pieChart.visibility = View.GONE
            return
        }

        binding.tvEmpty.visibility = View.GONE
        binding.pieChart.visibility = View.VISIBLE

        // 只显示前5个，其余归为"其他"
        val displayItems = if (items.size > 5) items.take(5) else items
        val otherMillis = if (items.size > 5) items.drop(5).sumOf { it.usageMillis } else 0L

        val entries = displayItems.map { item ->
            PieEntry(item.usageMillis / 60000f, item.appName) // 转换为分钟
        }
        if (otherMillis > 0) {
            entries.add(PieEntry(otherMillis / 60000f, "其他"))
        }

        val colors = listOf(
            Color.parseColor("#C62828"),
            Color.parseColor("#2E7D32"),
            Color.parseColor("#1565C0"),
            Color.parseColor("#F57F17"),
            Color.parseColor("#6A1B9A"),
            Color.parseColor("#757575")
        )

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors.take(entries.size)
            valueTextSize = 11f
            valueTextColor = Color.WHITE
            sliceSpace = 2f
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            holeRadius = 40f
            setCenterText("APP使用")
            centerTextColor = resources.getColor(com.google.android.material.R.color.material_dynamic_neutral95, null)
            legend.isEnabled = true
            legend.textSize = 12f
            animateY(500)
            invalidate()
        }
    }

    /**
     * 更新详细使用列表
     */
    private fun updateUsageList(items: List<StatsViewModel.AppUsageItem>) {
        val totalMillis = items.sumOf { it.usageMillis }
        val listView = binding.listViewUsage

        val data = items.map { item ->
            val percentage = if (totalMillis > 0) {
                (item.usageMillis.toFloat() / totalMillis * 100)
            } else 0f
            "${item.appName} — ${viewModel.formatDuration(item.usageMillis)} (${String.format("%.1f", percentage)}%)"
        }

        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            data
        )
        listView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
