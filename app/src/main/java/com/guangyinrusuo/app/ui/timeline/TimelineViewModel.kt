/*
 * 光阴如梭 - 个人效率工具
 * TimelineViewModel.kt
 *
 * 时间线模块 ViewModel — 0-24小时时间轴
 *
 * 新增功能：
 * - 支持 "09:00 标题" 格式解析并设 dueDate
 * - scrollToHour 用于添加任务后自动滚动到对应小时
 * - 按小时分组任务交由 Adapter 处理
 */

package com.guangyinrusuo.app.ui.timeline

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.guangyinrusuo.app.GuangYinRuSuoApplication
import com.guangyinrusuo.app.data.db.entity.TaskEntity
import kotlinx.coroutines.launch

class TimelineViewModel(application: Application) : AndroidViewModel(application) {

    private val taskRepository = (application as GuangYinRuSuoApplication).taskRepository

    // 所有任务列表（观察Room自动刷新）
    private val _allTasks = MutableLiveData<List<TaskEntity>>(emptyList())
    val allTasks: LiveData<List<TaskEntity>> = _allTasks

    // 添加任务后自动滚动到的小时
    private val _scrollToHour = MutableLiveData<Int?>(null)
    val scrollToHour: LiveData<Int?> = _scrollToHour

    // Snackbar 消息
    private val _snackbarMessage = MutableLiveData<String?>()
    val snackbarMessage: MutableLiveData<String?> = _snackbarMessage

    init {
        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            taskRepository.allTasks.collect { tasks ->
                _allTasks.value = tasks
            }
        }
    }

    /**
     * 添加新待办
     * @param title 任务标题
     * @param dueDate 可选的指定时间（毫秒时间戳），用于定位到时间轴上的具体小时
     */
    fun addTask(title: String, dueDate: Long? = null) {
        if (title.isBlank()) {
            _snackbarMessage.value = "请输入待办内容"
            return
        }

        viewModelScope.launch {
            try {
                taskRepository.addTask(title, dueDate = dueDate)
                _snackbarMessage.value = "已添加 ✓"
            } catch (e: Exception) {
                _snackbarMessage.value = "添加失败: ${e.message}"
            }
        }
    }

    fun markAsCompleted(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.markAsCompleted(task.id)
            _snackbarMessage.value = "已完成 ✓"
        }
    }

    fun unmarkAsCompleted(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.unmarkAsCompleted(task.id)
            _snackbarMessage.value = "已撤销"
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
            _snackbarMessage.value = "已删除"
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
            _snackbarMessage.value = "已更新"
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun getTodayCompletedCount(): Int {
        return _allTasks.value?.count { it.isCompleted } ?: 0
    }
}
