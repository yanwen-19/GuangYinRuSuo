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

    private val _allTasks = MutableLiveData<List<TaskEntity>>(emptyList())
    val allTasks: LiveData<List<TaskEntity>> = _allTasks

    private val _scrollToHour = MutableLiveData<Int?>(null)
    val scrollToHour: LiveData<Int?> = _scrollToHour

    private val _snackbarMessage = MutableLiveData<String?>()
    val snackbarMessage: MutableLiveData<String?> = _snackbarMessage

    init { viewModelScope.launch { taskRepository.allTasks.collect { _allTasks.value = it } } }

    fun addTask(title: String, dueDate: Long? = null) {
        if (title.isBlank()) { _snackbarMessage.value = "请输入待办内容"; return }
        viewModelScope.launch { try { taskRepository.addTask(title, dueDate = dueDate); _snackbarMessage.value = "已添加 ✓" } catch (e: Exception) { _snackbarMessage.value = "添加失败" } }
    }

    fun markAsCompleted(task: TaskEntity) { viewModelScope.launch { taskRepository.markAsCompleted(task.id); _snackbarMessage.value = "已完成 ✓" } }
    fun unmarkAsCompleted(task: TaskEntity) { viewModelScope.launch { taskRepository.unmarkAsCompleted(task.id); _snackbarMessage.value = "已撤销" } }
    fun deleteTask(task: TaskEntity) { viewModelScope.launch { taskRepository.deleteTask(task); _snackbarMessage.value = "已删除" } }
    fun updateTask(task: TaskEntity) { viewModelScope.launch { taskRepository.updateTask(task); _snackbarMessage.value = "已更新" } }
    fun clearSnackbar() { _snackbarMessage.value = null }
}
