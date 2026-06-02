package com.guangyinrusuo.app.adapter
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
class TaskAdapter : RecyclerView.Adapter<TaskAdapter.VH>() {
    class VH(v: View) : RecyclerView.ViewHolder(v)
    override fun getItemCount() = 0
    override fun onCreateViewHolder(p: ViewGroup, vt: Int) = VH(LayoutInflater.from(p.context).inflate(android.R.layout.simple_list_item_1, p, false))
    override fun onBindViewHolder(h: VH, p: Int) {}
}
