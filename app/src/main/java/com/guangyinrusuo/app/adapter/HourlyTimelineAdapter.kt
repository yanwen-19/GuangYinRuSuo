package com.guangyinrusuo.app.adapter
import androidx.recyclerview.widget.RecyclerView
class HourlyTimelineAdapter : RecyclerView.Adapter<HourlyTimelineAdapter.VH>() {
    class VH(v: android.view.View) : RecyclerView.ViewHolder(v)
    override fun getItemCount() = 0
    override fun onCreateViewHolder(p: android.view.ViewGroup, vt: Int) = VH(android.view.View(p.context))
    override fun onBindViewHolder(h: VH, p: Int) {}
}
