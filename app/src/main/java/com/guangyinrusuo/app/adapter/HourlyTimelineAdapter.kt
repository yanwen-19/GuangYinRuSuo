/*
 * 光阴如梭 - 个人效率工具
 * HourlyTimelineAdapter.kt
 *
 * 0-24小时时间轴适配器
 * 将24小时渲染为24行，每行左侧有时间标签，右侧显示该小时的任务卡片
 *
 * 核心逻辑：
 * - 数据源是 Map<Int, List<TaskEntity>>，key=小时(0-23)
 * - 如果没有任务，仍然渲染完整的时间轴（仅显示时间标签）
 * - 当前小时高亮显示
 * - 已完成任务左侧边框变绿
 */

package com.guangyinrusuo.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.guangyinrusuo.app.R
import com.guangyinrusuo.app.data.db.entity.TaskEntity
import com.guangyinrusuo.app.databinding.ItemTimelineHourBinding
import com.guangyinrusuo.app.databinding.ItemTimelineTaskBinding
import java.util.Calendar

/**
 * 按小时分组的数据类
 */
data class HourData(
    val hour: Int,          // 0-23
    val tasks: List<TaskEntity>,  // 该小时的任务（如果该小时没有任务，为空列表）
    val isCurrentHour: Boolean = false  // 是否为当前时间的小时
)

/**
 * 0-24小时时间轴适配器
 *
 * 固定24行（每行代表1小时），不管有没有任务
 * 每行由一个时间标签 + 0-N个任务卡片组成
 */
class HourlyTimelineAdapter(
    private val onTaskClick: (TaskEntity) -> Unit,
    private val onTaskToggleDone: (TaskEntity) -> Unit
) : RecyclerView.Adapter<HourlyTimelineAdapter.HourViewHolder>() {

    // 24小时数据
    private val hours = mutableListOf<HourData>()

    // 所有任务的引用（用于增删改查后重建数据）
    private var allTasks: List<TaskEntity> = emptyList()

    /**
     * 设置新数据并刷新
     */
    fun setTasks(tasks: List<TaskEntity>) {
        allTasks = tasks
        rebuildHourData()
    }

    /**
     * 重建24小时数据
     * 将任务按小时分组，空的也要保留小时行
     */
    private fun rebuildHourData() {
        hours.clear()
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)

        // 将任务按小时分组
        val taskMap = mutableMapOf<Int, MutableList<TaskEntity>>()
        for (task in allTasks) {
            // 根据dueDate或createdAt确定小时
            val taskHour = getTaskHour(task)
            taskMap.getOrPut(taskHour) { mutableListOf() }.add(task)
        }

        // 构建24小时数据，空的也要加
        for (h in 0..23) {
            val tasksAtHour = taskMap[h]?.sortedBy {
                getTaskMinute(it)  // 按分钟排序
            } ?: emptyList()

            hours.add(
                HourData(
                    hour = h,
                    tasks = tasksAtHour,
                    isCurrentHour = h == currentHour
                )
            )
        }
        notifyDataSetChanged()
    }

    /**
     * 获取任务所在的小时
     * 优先使用dueDate（用户指定的时间），否则用createdAt
     */
    private fun getTaskHour(task: TaskEntity): Int {
        val timeMs = if (task.dueDate != null && task.dueDate!! > 0) {
            task.dueDate!!
        } else {
            task.createdAt
        }
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMs
        return cal.get(Calendar.HOUR_OF_DAY)
    }

    /**
     * 获取任务的具体分钟
     */
    private fun getTaskMinute(task: TaskEntity): Int {
        val timeMs = if (task.dueDate != null && task.dueDate!! > 0) {
            task.dueDate!!
        } else {
            task.createdAt
        }
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMs
        return cal.get(Calendar.MINUTE)
    }

    override fun getItemCount(): Int = 24

    override fun onBindViewHolder(holder: HourViewHolder, position: Int) {
        holder.bind(hours[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourViewHolder {
        val binding = ItemTimelineHourBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HourViewHolder(binding, onTaskClick, onTaskToggleDone)
    }

    /**
     * ViewHolder — 每行代表1小时
     * 动态向llHourContent中添加任务卡片
     */
    class HourViewHolder(
        private val binding: ItemTimelineHourBinding,
        private val onTaskClick: (TaskEntity) -> Unit,
        private val onTaskToggleDone: (TaskEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(hourData: HourData) {
            val context = binding.root.context

            // 设置时间标签
            val timeStr = String.format("%02d:00", hourData.hour)
            binding.tvHourLabel.text = timeStr

            // 当前小时高亮
            if (hourData.isCurrentHour) {
                binding.tvHourLabel.setTextColor(
                    ContextCompat.getColor(context, R.color.pomo_focus)
                )
                binding.tvHourLabel.text = "此时"
            } else {
                binding.tvHourLabel.setTextColor(
                    ContextCompat.getColor(context, R.color.timeline_line_color)
                )
            }

            // 清除之前添加的任务卡片
            binding.llHourContent.removeAllViews()

            // 为每个任务创建卡片
            for (task in hourData.tasks) {
                val taskBinding = ItemTimelineTaskBinding.inflate(
                    LayoutInflater.from(context),
                    binding.llHourContent,
                    false
                )

                // 设置标题
                taskBinding.tvTaskTitle.text = task.title

                // 已完成/未完成状态
                if (task.isCompleted) {
                    taskBinding.tvStatusIcon.text = "✓"
                    taskBinding.tvStatusIcon.setTextColor(
                        ContextCompat.getColor(context, R.color.task_completed)
                    )
                    taskBinding.tvTaskTitle.setTextColor(
                        ContextCompat.getColor(context, R.color.task_completed_text)
                    )
                    taskBinding.root.alpha = 0.65f
                } else {
                    taskBinding.tvStatusIcon.text = "○"
                    taskBinding.tvStatusIcon.setTextColor(
                        ContextCompat.getColor(context, R.color.pomo_focus)
                    )
                    taskBinding.tvTaskTitle.setTextColor(
                        ContextCompat.getColor(context, R.color.material_dynamic_neutral95)
                    )
                    taskBinding.root.alpha = 1f
                }

                // 设置分钟时间
                val cal = Calendar.getInstance()
                val timeMs = if (task.dueDate != null && task.dueDate!! > 0) {
                    task.dueDate!!
                } else {
                    task.createdAt
                }
                cal.timeInMillis = timeMs
                taskBinding.tvTaskMinute.text = String.format(
                    "%02d:%02d",
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE)
                )

                // 点击任务
                taskBinding.root.setOnClickListener {
                    onTaskClick(task)
                }

                // 长按切换完成状态
                taskBinding.root.setOnLongClickListener {
                    onTaskToggleDone(task)
                    true
                }

                binding.llHourContent.addView(taskBinding.root)
            }
        }
    }
}
