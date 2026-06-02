package com.guangyinrusuo.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.guangyinrusuo.app.R
import com.guangyinrusuo.app.data.db.entity.TaskEntity
import com.guangyinrusuo.app.databinding.ItemTimelineHourBinding
import com.guangyinrusuo.app.databinding.ItemTimelineTaskBinding
import java.util.Calendar

data class HourData(val hour: Int, val tasks: List<TaskEntity>, val isCurrentHour: Boolean = false)

class HourlyTimelineAdapter(
    private val onTaskClick: (TaskEntity) -> Unit,
    private val onTaskToggleDone: (TaskEntity) -> Unit
) : RecyclerView.Adapter<HourlyTimelineAdapter.HourViewHolder>() {

    private val hours = mutableListOf<HourData>()
    private var allTasks: List<TaskEntity> = emptyList()

    fun setTasks(tasks: List<TaskEntity>) { allTasks = tasks; rebuildHourData() }

    private fun rebuildHourData() {
        hours.clear()
        val n = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val m = mutableMapOf<Int, MutableList<TaskEntity>>()
        for (t in allTasks) { val cal = Calendar.getInstance(); cal.timeInMillis = if (t.dueDate != null && t.dueDate!! > 0) t.dueDate!! else t.createdAt; m.getOrPut(cal.get(Calendar.HOUR_OF_DAY)) { mutableListOf() }.add(t) }
        for (h in 0..23) hours.add(HourData(h, m[h]?.sortedBy { val c=Calendar.getInstance(); c.timeInMillis=it.createdAt; c.get(Calendar.MINUTE) } ?: emptyList(), h==n))
        notifyDataSetChanged()
    }

    override fun getItemCount() = 24
    override fun onBindViewHolder(holder: HourViewHolder, p: Int) { holder.bind(hours[p]) }
    override fun onCreateViewHolder(p: ViewGroup, vt: Int) = HourViewHolder(ItemTimelineHourBinding.inflate(LayoutInflater.from(p.context), p, false), onTaskClick, onTaskToggleDone)

    class HourViewHolder(private val b: ItemTimelineHourBinding, private val cl: (TaskEntity)->Unit, private val tg: (TaskEntity)->Unit) : RecyclerView.ViewHolder(b.root) {
        fun bind(h: HourData) {
            val ctx = b.root.context
            b.tvHourLabel.text = if (h.isCurrentHour) "此时" else String.format("%02d:00", h.hour)
            b.tvHourLabel.setTextColor(ContextCompat.getColor(ctx, if (h.isCurrentHour) R.color.pomo_focus else R.color.timeline_line_color))
            b.llHourContent.removeAllViews()
            for (t in h.tasks) {
                val tb = ItemTimelineTaskBinding.inflate(LayoutInflater.from(ctx), b.llHourContent, false)
                tb.tvTaskTitle.text = t.title
                if (t.isCompleted) { tb.tvStatusIcon.text = "✓"; tb.tvStatusIcon.setTextColor(ContextCompat.getColor(ctx, R.color.task_completed)); tb.tvTaskTitle.setTextColor(ContextCompat.getColor(ctx, R.color.task_completed_text)); tb.root.alpha = 0.65f }
                else { tb.tvStatusIcon.text = "○"; tb.tvStatusIcon.setTextColor(ContextCompat.getColor(ctx, R.color.pomo_focus)); tb.tvTaskTitle.setTextColor(ContextCompat.getColor(ctx, R.color.timeline_line_color)); tb.root.alpha = 1f }
                val cal = Calendar.getInstance(); cal.timeInMillis = if (t.dueDate != null && t.dueDate!! > 0) t.dueDate!! else t.createdAt
                tb.tvTaskMinute.text = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                tb.root.setOnClickListener { cl(t) }; tb.root.setOnLongClickListener { tg(t); true }
                b.llHourContent.addView(tb.root)
            }
        }
    }
}
