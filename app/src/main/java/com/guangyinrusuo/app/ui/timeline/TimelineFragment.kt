/*
 * 光阴如梭 - 个人效率工具
 * TimelineFragment.kt
 *
 * 时间线页面Fragment — 0-24小时坐标轴视图
 *
 * 功能：
 * 1. 顶部输入框支持 "09:00 背单词" 格式自动解析时间
 * 2. 主区域为0-24小时时间轴滚动列表
 * 3. 每个任务按时间显示在对应小时行中
 * 4. 无任务时仍然显示完整的时间轴标尺
 * 5. 当前小时有醒目指示
 * 6. 点击任务切换完成/未完成，长按删除
 */

package com.guangyinrusuo.app.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.guangyinrusuo.app.adapter.HourlyTimelineAdapter
import com.guangyinrusuo.app.data.db.entity.TaskEntity
import com.guangyinrusuo.app.databinding.FragmentTimelineBinding

/**
 * 时间线Fragment — 0-24小时时间轴
 */
class TimelineFragment : Fragment() {

    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TimelineViewModel
    private lateinit var adapter: HourlyTimelineAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[TimelineViewModel::class.java]

        setupRecyclerView()
        setupInputArea()
        observeData()
        observeSnackbar()
    }

    /**
     * 配置 RecyclerView — 使用 HourlyTimelineAdapter
     * 固定24行，每行代表1小时
     */
    private fun setupRecyclerView() {
        adapter = HourlyTimelineAdapter(
            onTaskClick = { task -> toggleTaskCompletion(task) },
            onTaskToggleDone = { task -> toggleTaskCompletion(task) }
        )

        binding.rvTimeline.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TimelineFragment.adapter
        }
    }

    /**
     * 配置输入框和添加按钮
     * 输入格式： "09:00 背50个单词" → 自动解析09:00为时间
     *            "随便写个待办" → 使用当前时间
     */
    private fun setupInputArea() {
        binding.btnAddTask.setOnClickListener { addTaskFromInput() }
        binding.etTaskInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addTaskFromInput(); true
            } else false
        }
    }

    /**
     * 解析输入并添加待办
     * 解析逻辑：尝试匹配 "HH:mm XXXX" 格式
     */
    private fun addTaskFromInput() {
        val raw = binding.etTaskInput.text.toString().trim()
        if (raw.isEmpty()) return

        // 尝试解析时间格式 "09:00 背50个单词"
        val timePattern = Regex("""^(\d{1,2}):(\d{2})\s+(.+)$""")
        val match = timePattern.find(raw)

        if (match != null) {
            val hour = match.groupValues[1].toInt()
            val minute = match.groupValues[2].toInt()
            val title = match.groupValues[3].trim()

            if (hour in 0..23 && minute in 0..59) {
                // 计算今天这个时间的时间戳
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
                cal.set(java.util.Calendar.MINUTE, minute)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                viewModel.addTask(title, dueDate = cal.timeInMillis)
                binding.etTaskInput.text?.clear()

                // 添加后自动滚动到该小时
                viewModel.scrollToHour.postValue(hour)
                return
            }
        }

        // 没有解析出时间，使用当前时间
        viewModel.addTask(raw)
        binding.etTaskInput.text?.clear()
    }

    /**
     * 切换完成/未完成
     */
    private fun toggleTaskCompletion(task: TaskEntity) {
        if (task.isCompleted) {
            viewModel.unmarkAsCompleted(task)
        } else {
            viewModel.markAsCompleted(task)
        }
    }

    /**
     * 观察数据 — 将任务列表传给适配器
     * 适配器内部会按小时分组并渲染24行
     */
    private fun observeData() {
        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            adapter.setTasks(tasks)
        }

        // 自动滚动到指定小时
        viewModel.scrollToHour.observe(viewLifecycleOwner) { hour ->
            hour?.let {
                binding.rvTimeline.post {
                    binding.rvTimeline.scrollToPosition(it.coerceIn(0, 23))
                }
                viewModel.scrollToHour.postValue(null)
            }
        }
    }

    private fun observeSnackbar() {
        viewModel.snackbarMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearSnackbar()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
