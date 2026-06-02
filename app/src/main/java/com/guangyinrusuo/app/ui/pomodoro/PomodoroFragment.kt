/*
 * 光阴如梭 - 个人效率工具
 * PomodoroFragment.kt
 *
 * 番茄钟页面Fragment
 * 显示倒计时UI、控制按钮、今日专注统计
 *
 * 与 PomoForegroundService 配合，确保后台倒计时不被系统杀死
 * 使用 PomodoroViewModel 管理状态和计时逻辑
 */

package com.guangyinrusuo.app.ui.pomodoro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.guangyinrusuo.app.R
import com.guangyinrusuo.app.data.db.entity.TaskEntity
import com.guangyinrusuo.app.databinding.FragmentPomodoroBinding

/**
 * 番茄钟Fragment
 */
class PomodoroFragment : Fragment() {

    private var _binding: FragmentPomodoroBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PomodoroViewModel

    // 任务选择适配器
    private var taskList = mutableListOf<TaskEntity>()
    private lateinit var taskAdapter: android.widget.ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPomodoroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[PomodoroViewModel::class.java]

        // 设置控制按钮
        setupButtons()

        // 设置关联任务选择器
        setupTaskSpinner()

        // 观察ViewModels数据变化
        observeState()
        observeTimer()
        observeStats()
    }

    private fun setupButtons() {
        // 开始/暂停按钮
        binding.btnStartPause.setOnClickListener {
            when (viewModel.state.value) {
                PomoState.IDLE -> {
                    // 开始专注
                    val selectedTaskId = getSelectedTaskId()
                    viewModel.startFocus(selectedTaskId)
                }
                PomoState.PAUSED -> viewModel.resumeFocus()
                PomoState.FOCUSING -> viewModel.pauseFocus()
                PomoState.BREAK -> viewModel.pauseFocus() // 休息中点击暂停
                else -> {}
            }
        }

        // 停止按钮
        binding.btnStop.setOnClickListener {
            viewModel.stopFocus()
        }
    }

    /**
     * 设置关联任务下拉选择器
     */
    private fun setupTaskSpinner() {
        val taskNames = mutableListOf<String>()
        taskNames.add("不限关联任务")
        taskAdapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            taskNames
        )
        taskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTask.adapter = taskAdapter

        // 加载未完成任务列表
        viewModel.loadUncompletedTasks { tasks ->
            taskList.clear()
            taskList.addAll(tasks)
            taskNames.clear()
            taskNames.add("不限关联任务")
            taskNames.addAll(tasks.map { it.title })
            taskAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 获取选择的关联任务ID
     */
    private fun getSelectedTaskId(): Long? {
        val position = binding.spinnerTask.selectedItemPosition
        return if (position > 0 && position <= taskList.size) {
            taskList[position - 1].id
        } else null
    }

    /**
     * 观察番茄钟状态变化
     */
    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                PomoState.IDLE -> {
                    binding.btnStartPause.text = "开始专注"
                    binding.btnStartPause.setIconResource(R.drawable.ic_play)
                    binding.btnStop.visibility = View.GONE
                    binding.chipState.text = "准备就绪"
                    binding.chipState.setChipBackgroundColorResource(R.color.pomo_idle)
                    binding.spinnerTask.isEnabled = true
                }
                PomoState.FOCUSING -> {
                    binding.btnStartPause.text = "暂停"
                    binding.btnStartPause.setIconResource(R.drawable.ic_pause)
                    binding.btnStop.visibility = View.VISIBLE
                    binding.chipState.text = "专注中…"
                    binding.chipState.setChipBackgroundColorResource(R.color.pomo_focus)
                    binding.spinnerTask.isEnabled = false
                }
                PomoState.PAUSED -> {
                    binding.btnStartPause.text = "继续"
                    binding.btnStartPause.setIconResource(R.drawable.ic_play)
                    binding.chipState.text = "已暂停"
                    binding.chipState.setChipBackgroundColorResource(R.color.pomo_idle)
                }
                PomoState.BREAK -> {
                    binding.btnStartPause.text = "暂停"
                    binding.btnStartPause.setIconResource(R.drawable.ic_pause)
                    binding.btnStop.visibility = View.VISIBLE
                    binding.chipState.text = "休息中…"
                    binding.chipState.setChipBackgroundColorResource(R.color.pomo_break)
                    binding.spinnerTask.isEnabled = false
                }
            }
        }
    }

    /**
     * 观察倒计时数字变化
     */
    private fun observeTimer() {
        viewModel.remainingSeconds.observe(viewLifecycleOwner) { seconds ->
            binding.tvTimer.text = viewModel.getFormattedTime(seconds)
        }
    }

    /**
     * 观察今日统计变化
     */
    private fun observeStats() {
        viewModel.todayFocusSeconds.observe(viewLifecycleOwner) { seconds ->
            val minutes = seconds / 60
            binding.tvTodayFocus.text = if (minutes >= 60) {
                "${minutes / 60}小时${minutes % 60}分钟"
            } else {
                "${minutes}分钟"
            }
        }

        viewModel.todaySessionCount.observe(viewLifecycleOwner) { count ->
            binding.tvTodaySessions.text = count.toString()
            binding.tvSessionCount.text = "今日完成: $count 个"
        }
    }

    override fun onResume() {
        super.onResume()
        // 页面可见时刷新统计（可能在其他页面或Widget中完成番茄钟）
        viewModel.loadTodayStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
