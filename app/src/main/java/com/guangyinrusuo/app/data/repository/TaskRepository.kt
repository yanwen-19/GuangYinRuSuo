/*
 * 光阴如梭 - 个人效率工具
 * TaskRepository.kt
 *
 * 待办事项数据仓库
 * 作为ViewModel和DAO之间的唯一数据来源(Single Source of Truth)
 * ViewModel不直接操作DAO，统一通过Repository访问数据
 *
 * 职责：
 * 1. 封装DAO操作
 * 2. 提供协程安全的数据库访问
 * 3. 数据转换/格式化（如需要）
 */

package com.guangyinrusuo.app.data.repository

import com.guangyinrusuo.app.data.db.dao.TaskDao
import com.guangyinrusuo.app.data.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * TaskRepository — 待办事项数据仓库
 *
 * @param taskDao Room DAO实例，通过Application获取
 */
class TaskRepository(private val taskDao: TaskDao) {

    /**
     * 观察所有待办（实时更新）
     */
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    /**
     * 观察未完成待办
     */
    val uncompletedTasks: Flow<List<TaskEntity>> = taskDao.getUncompletedTasks()

    /**
     * 观察已完成待办
     */
    val completedTasks: Flow<List<TaskEntity>> = taskDao.getCompletedTasks()

    /**
     * 根据ID获取单个待办
     */
    suspend fun getTaskById(taskId: Long): TaskEntity? {
        return taskDao.getTaskById(taskId)
    }

    /**
     * 添加新待办
     * @return 新记录的ID
     */
    suspend fun addTask(title: String, description: String? = null, dueDate: Long? = null): Long {
        val task = TaskEntity(
            title = title.trim(),
            description = description,
            dueDate = dueDate,
            createdAt = System.currentTimeMillis()
        )
        return taskDao.insertTask(task)
    }

    /**
     * 更新待办
     */
    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    /**
     * 删除待办
     */
    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    /**
     * 标记完成
     */
    suspend fun markAsCompleted(taskId: Long) {
        taskDao.markAsCompleted(taskId, System.currentTimeMillis())
    }

    /**
     * 撤销完成
     */
    suspend fun unmarkAsCompleted(taskId: Long) {
        taskDao.unmarkAsCompleted(taskId)
    }

    /**
     * 获取某时间段内完成的任务（用于每日复盘）
     */
    suspend fun getTasksCompletedBetween(dayStart: Long, dayEnd: Long): List<TaskEntity> {
        return taskDao.getTasksCompletedBetween(dayStart, dayEnd)
    }

    /**
     * 获取今日任务统计
     */
    suspend fun getTodayStats(todayStart: Long): Pair<Int, Int> {
        val total = taskDao.getTodayTaskCount(todayStart)
        val completed = taskDao.getTodayCompletedCount(todayStart)
        return Pair(total, completed)
    }
}
