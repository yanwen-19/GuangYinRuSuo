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
import com.guangyinrusuo.app.databinding.FragmentAppUsageStatsBinding

class AppUsageStatsFragment : Fragment() {
    private var _b: FragmentAppUsageStatsBinding? = null
    private val b get() = _b!!
    private lateinit var vm: StatsViewModel

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View { _b = FragmentAppUsageStatsBinding.inflate(i, c, false); return b.root }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        vm = ViewModelProvider(requireParentFragment())[StatsViewModel::class.java]
        vm.appUsageList.observe(viewLifecycleOwner) { items -> updatePie(items); updateList(items) }
    }
    private fun updatePie(items: List<StatsViewModel.AppUsageItem>) {
        if (items.isEmpty()) { b.tvEmpty.visibility = View.VISIBLE; b.pieChart.visibility = View.GONE; return }
        b.tvEmpty.visibility = View.GONE; b.pieChart.visibility = View.VISIBLE
        val d = if (items.size > 5) items.take(5) else items; val o = if (items.size > 5) items.drop(5).sumOf { it.usageMillis } else 0L
        val e = mutableListOf<PieEntry>(); d.forEach { e.add(PieEntry(it.usageMillis/60000f, it.appName)) }; if (o>0) e.add(PieEntry(o/60000f, "其他"))
        val ds = PieDataSet(e, "").apply { colors = listOf(Color.parseColor("#C62828"),Color.parseColor("#2E7D32"),Color.parseColor("#1565C0"),Color.parseColor("#F57F17"),Color.parseColor("#6A1B9A"),Color.parseColor("#757575")).take(e.size); valueTextSize=11f; sliceSpace=2f }
        b.pieChart.apply { data=PieData(ds); description.isEnabled=false; setUsePercentValues(true); isDrawHoleEnabled=true; holeRadius=40f; setCenterText("APP使用"); legend.isEnabled=true; legend.textSize=12f; animateY(500); invalidate() }
    }
    private fun updateList(items: List<StatsViewModel.AppUsageItem>) {
        val t = items.sumOf { it.usageMillis }
        val d = items.map { "${it.appName} — ${vm.formatDuration(it.usageMillis)} (${String.format("%.1f", if (t>0) it.usageMillis.toFloat()/t*100 else 0f)}%)" }
        requireActivity().runOnUiThread { b.listViewUsage.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, d) }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
