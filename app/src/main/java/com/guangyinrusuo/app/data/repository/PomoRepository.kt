/*
 * 光阴如梭 - 个人效率工具
 * PomoRepository.kt
 *
 * 番茄钟记录数据仓库
 * 封装 PomoSessionDao 的所有操作
 */

package com.guangyinrusuo.app.data.repository

import com.guangyinrusuo.app.data.db.dao.PomoSessionDao
import com.guangyinrusuo.app.data.db.entity.PomoSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 番茄钟记录数据仓库
 */
class PomoRepository(private val dao: PomoSessionDao) {

    /**
     * 所有番茄钟记录（实时）
     */
    val allSessions: Flow<List<PomoSessionEntity>> = dao.getAllSessions()

    /**
     * 获取最近N条记录
     */
    suspend fun getRecentSessions(limit: Int = 10): List<PomoSessionEntity> {
        return dao.getRecentSessions(limit)
    }

    /**
     * 开始新番茄钟 — 插入一条记录
     * @return 新记录的ID
     */
    suspend fun startSession(type: String = "focus", taskId: Long? = null): Long {
        val session = PomoSessionEntity(
            startTime = System.currentTimeMillis(),
            durationSeconds = 0,
            type = type,
            taskId = taskId
        )
        return dao.insertSession(session)
    }

    /**
     * 结束番茄钟 — 更新结束时间和实际秒数
     */
    suspend fun finishSession(sessionId: Long, actualSeconds: Int) {
        dao.updateSessionEnd(sessionId, System.currentTimeMillis(), actualSeconds)
    }

    /**
     * 获取某个时间段内的专注记录
     */
    suspend fun getFocusSessionsBetween(startTime: Long, endTime: Long): List<PomoSessionEntity> {
        return dao.getFocusSessionsBetween(startTime, endTime)
    }

    /**
     * 获取某天的总专注秒数
     */
    suspend fun getTotalFocusSeconds(dayStart: Long, dayEnd: Long): Int {
        return dao.getTotalFocusSecondsBetween(dayStart, dayEnd)
    }

    /**
     * 获取某天完成番茄钟数量
     */
    suspend fun getFocusSessionCount(dayStart: Long, dayEnd: Long): Int {
        return dao.getFocusSessionCountBetween(dayStart, dayEnd)
    }

    /**
     * 获取最近7天的每日专注数据（用于统计柱状图）
     * 返回 List<Pair<日期字符串, 专注秒数>>
     */
    suspend fun getWeeklyFocusData(): List<Pair<String, Int>> {
        val calendar = java.util.Calendar.getInstance()
        val data = mutableListOf<Pair<String, Int>>()

        // 从6天前开始，到今天，共7天
        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -i)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val dayStart = calendar.timeInMillis

            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            val dayEnd = calendar.timeInMillis

            val seconds = dao.getTotalFocusSecondsBetween(dayStart, dayEnd)
            val dateStr = java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault())
                .format(java.util.Date(dayStart))
            data.add(Pair(dateStr, seconds))
        }
        return data
    }

    /**
     * 删除一条记录
     */
    suspend fun deleteSession(session: PomoSessionEntity) {
        dao.deleteSession(session)
    }
}
