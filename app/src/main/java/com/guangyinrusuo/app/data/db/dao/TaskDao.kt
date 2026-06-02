/*
 * 光阴如梭 - 个人效率工具
 * TaskDao.kt
 *
 * 待办事项数据访问对象 (Data Access Object)
 * 定义对 tasks 表的所有数据库操作
 *
 * Room DAO 使用注解标记SQL操作：
 * @Insert / @Update / @Delete — 自动生成SQL
 * @Query — 自定义SQL查询 (编译时校验SQL语法)
 *
 * 所有耗时数据库操作默认在后台线程执行，
 * 配合 suspend 关键字确保可以从协程中调用
 */

package com.guangyinrusuo.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.guangyinrusuo.app.data.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * 待办事项数据访问对象
 *
 * Flow 返回值 = Room 自动在数据变化时推送最新结果
 * 非常适合 UI层通过 LiveData/Flow 观察数据变化自动刷新
 */
@Dao
interface TaskDao {

    /**
     * 获取所有待办事项，按创建时间降序排列
     * Flow 返回值 → 数据变化时自动通知订阅者
     */
    @Query("SELECT * FROM tasks ORDER BY sort_order ASC, created_at DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    /**
     * 获取未完成的待办事项（用于时间线展示）
     */
    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY sort_order ASC, created_at DESC")
    fun getUncompletedTasks(): Flow<List<TaskEntity>>

    /**
     * 获取已完成的待办事项（用于时间线回放/复盘）
     */
    @Query("SELECT * FROM tasks WHERE is_completed = 1 ORDER BY completed_at DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>

    /**
     * 根据ID获取单个待办
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    /**
     * 插入一条新待办
     * @return 插入记录的ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    /**
     * 更新待办
     */
    @Update
    suspend fun updateTask(task: TaskEntity)

    /**
     * 删除待办
     */
    @Delete
    suspend fun deleteTask(task: TaskEntity)

    /**
     * 根据ID删除待办
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    /**
     * 标记待办为已完成
     * @param taskId 待办ID
     * @param completedAt 完成时间戳
     */
    @Query("UPDATE tasks SET is_completed = 1, completed_at = :completedAt WHERE id = :taskId")
    suspend fun markAsCompleted(taskId: Long, completedAt: Long = System.currentTimeMillis())

    /**
     * 撤销完成状态（重新标记为未完成）
     */
    @Query("UPDATE tasks SET is_completed = 0, completed_at = NULL WHERE id = :taskId")
    suspend fun unmarkAsCompleted(taskId: Long)

    /**
     * 获取某天的已完成任务（用于时间线回放）
     */
    @Query("""
        SELECT * FROM tasks
        WHERE is_completed = 1
        AND completed_at >= :dayStart
        AND completed_at < :dayEnd
        ORDER BY completed_at ASC
    """)
    suspend fun getTasksCompletedBetween(dayStart: Long, dayEnd: Long): List<TaskEntity>

    /**
     * 获取今天的待办总数
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE created_at >= :todayStart")
    suspend fun getTodayTaskCount(todayStart: Long): Int

    /**
     * 获取今天的完成任务数
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE completed_at >= :todayStart")
    suspend fun getTodayCompletedCount(todayStart: Long): Int
}
