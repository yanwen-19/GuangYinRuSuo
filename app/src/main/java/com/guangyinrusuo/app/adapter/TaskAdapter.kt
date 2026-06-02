/*
 * 光阴如梭 - 个人效率工具
 * TaskAdapter.kt
 *
 * 时间线待办列表的 RecyclerView 适配器
 * 使用 ListAdapter + DiffUtil 实现高效列表更新
 * 自动识别哪些item发生了变化，只刷新变化的部分
 *
 * 时间轴UI通过左侧的节点圆点+竖线实现
 * 已完成的任务节点变为绿色，标题有删除线
 */

package com.guangyinrusuo.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.guangyinrusuo.app.R
import com.guangyinrusuo.app.data.db.entity.TaskEntity
import com.guangyinrusuo.app.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 待办事项列表适配器
 *
 * @param onItemClick 点击item回调（可用于打开编辑）
 * @param onDoneClick 标记完成/撤销完成回调
 * @param onDeleteClick 删除回调
 */
class TaskAdapter(
    private val onItemClick: (TaskEntity) -> Unit,
    private val onDoneClick: (TaskEntity) -> Unit,
    private val onDeleteClick: (TaskEntity) -> Unit
) : ListAdapter<TaskEntity, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    // 时间格式化器
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder 持有 item_task.xml 中所有控件的引用
     */
    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: TaskEntity) {
            // 设置标题
            binding.tvTaskTitle.text = task.title

            // 根据完成状态切换样式
            if (task.isCompleted) {
                // 已完成：节点变绿、标题变灰+删除线
                binding.ivNode.setImageResource(R.drawable.bg_timeline_node_done)
                binding.tvTaskTitle.setTextColor(
                    binding.root.context.getColor(R.color.task_completed_text)
                )
                binding.tvTaskTitle.paint.isStrikeThruText = true
            } else {
                // 未完成：红色节点、正常文字
                binding.ivNode.setImageResource(R.drawable.bg_timeline_node)
                binding.tvTaskTitle.setTextColor(
                    binding.root.context.getColor(com.google.android.material.R.color.material_dynamic_neutral95)
                )
                binding.tvTaskTitle.paint.isStrikeThruText = false
            }

            // 显示时间
            binding.tvTaskTime.text = if (task.isCompleted && task.completedAt != null) {
                "完成于 ${timeFormat.format(Date(task.completedAt))}"
            } else {
                "创建于 ${dateTimeFormat.format(Date(task.createdAt))}"
            }

            // 按钮点击事件
            binding.btnTaskDone.setOnClickListener { onDoneClick(task) }
            binding.btnTaskDelete.setOnClickListener { onDeleteClick(task) }

            // 整个item点击（用于编辑）
            binding.root.setOnClickListener { onItemClick(task) }
        }
    }

    /**
     * DiffUtil 回调 — 告诉 RecyclerView 如何比较两个列表项的异同
     * 这大幅提升了列表刷新性能，避免调用 notifyDataSetChanged()
     */
    class TaskDiffCallback : DiffUtil.ItemCallback<TaskEntity>() {
        override fun areItemsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
            // 根据ID判断是否为同一个待办
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
            // 判断内容是否有变化（决定是否需要重新渲染）
            return oldItem == newItem
        }
    }
}
