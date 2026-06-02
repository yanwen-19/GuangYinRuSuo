/*
 * 光阴如梭 - 个人效率工具
 * PomoSessionDao.kt
 *
 * 番茄钟记录数据访问对象
 * 定义对 pomo_sessions 表的所有操作
 *
 * 关键查询：
 * - 按时间范围统计专注时长 (日/周统计视图用)
 * - 按类型筛选 (专注/休息)
 * - 获取最近N条记录 (时间线展示)
 */

package com.guangyinrusuo.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guangyinrusuo.app.data.db.entity.PomoSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 番茄钟记录DAO
 */
@Dao
interface PomoSessionDao {

    /**
     * 获取所有番茄钟记录（按开始时间降序）
     */
    @Query("SELECT * FROM pomo_sessions ORDER BY start_time DESC")
    fun getAllSessions(): Flow<List<PomoSessionEntity>>

    /**
     * 获取最近N条番茄钟记录
     */
    @Query("SELECT * FROM pomo_sessions ORDER BY start_time DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int): List<PomoSessionEntity>

    /**
     * 获取指定时间范围内的专注记录
     * 用于日/周统计视图
     */
    @Query("""
        SELECT * FROM pomo_sessions
        WHERE type = 'focus'
        AND start_time >= :startTime
        AND start_time <= :endTime
        ORDER BY start_time ASC
    """)
    suspend fun getFocusSessionsBetween(startTime: Long, endTime: Long): List<PomoSessionEntity>

    /**
     * 获取指定日期内专注总时长（秒）
     */
    @Query("""
        SELECT COALESCE(SUM(duration_seconds), 0)
        FROM pomo_sessions
        WHERE type = 'focus'
        AND start_time >= :dayStart
        AND start_time <= :dayEnd
    """)
    suspend fun getTotalFocusSecondsBetween(dayStart: Long, dayEnd: Long): Int

    /**
     * 获取指定日期内完成番茄钟数量
     */
    @Query("""
        SELECT COUNT(*)
        FROM pomo_sessions
        WHERE type = 'focus'
        AND start_time >= :dayStart
        AND start_time <= :dayEnd
    """)
    suspend fun getFocusSessionCountBetween(dayStart: Long, dayEnd: Long): Int

    /**
     * 获取最近一周每天的总专注时长（用于柱状图）
     * 注意：这个查询需要配合外部循环或多次调用，
     * Room原生不支持按天GROUP BY里的时间格式化，
     * 但我们可以在Repository层循环调用getTotalFocusSecondsBetween
     */

    /**
     * 插入一条番茄钟记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomoSessionEntity): Long

    /**
     * 更新番茄钟记录（如结束时更新 endTime）
     */
    @Query("""
        UPDATE pomo_sessions
        SET end_time = :endTime, duration_seconds = :durationSeconds
        WHERE id = :sessionId
    """)
    suspend fun updateSessionEnd(sessionId: Long, endTime: Long, durationSeconds: Int)

    /**
     * 删除一条记录
     */
    @Delete
    suspend fun deleteSession(session: PomoSessionEntity)

    /**
     * 获取最近的未完成记录（用于恢复中断的番茄钟）
     */
    @Query("SELECT * FROM pomo_sessions WHERE end_time IS NULL ORDER BY start_time DESC LIMIT 1")
    suspend fun getLatestIncompleteSession(): PomoSessionEntity?
}
