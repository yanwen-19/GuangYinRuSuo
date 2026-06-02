/*
 * 光阴如梭 - 个人效率工具
 * UsageRepository.kt
 *
 * APP使用记录数据仓库
 * 封装 AppUsageDao 操作，提供统计模块和监控模块的数据支持
 */

package com.guangyinrusuo.app.data.repository

import com.guangyinrusuo.app.data.db.dao.AppUsageDao
import com.guangyinrusuo.app.data.db.entity.AppUsageEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * APP使用记录数据仓库
 */
class UsageRepository(private val dao: AppUsageDao) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * 获取今天的日期字符串
     */
    private fun getTodayDate(): String = dateFormat.format(Date())

    /**
     * 获取指定日期的所有APP使用记录
     */
    suspend fun getRecordsByDate(date: String? = null): List<AppUsageEntity> {
        return dao.getRecordsByDate(date ?: getTodayDate())
    }

    /**
     * 保存或更新APP使用记录
     */
    suspend fun saveRecord(record: AppUsageEntity) {
        dao.insertOrUpdate(record)
    }

    /**
     * 批量保存记录（WorkManager调用）
     */
    suspend fun saveAllRecords(records: List<AppUsageEntity>) {
        dao.insertAll(records)
    }

    /**
     * 获取今天某个应用的使用时长（毫秒）
     */
    suspend fun getAppUsageToday(packageName: String): Long {
        return dao.getAppUsageToday(packageName, getTodayDate()) ?: 0L
    }

    /**
     * 获取今天总使用时长（毫秒）
     */
    suspend fun getTodayTotalUsage(): Long {
        return dao.getTodayTotalUsage(getTodayDate())
    }

    /**
     * 获取今天的各APP使用数据列表
     */
    suspend fun getTodayUsageList(): List<AppUsageEntity> {
        return dao.getRecordsByDate(getTodayDate())
    }

    /**
     * 获取近7天的APP使用数据列表
     */
    suspend fun getWeeklyUsageRecords(): List<AppUsageEntity> {
        val calendar = java.util.Calendar.getInstance()
        val dates = mutableListOf<String>()

        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -i)
            dates.add(dateFormat.format(calendar.time))
        }

        return dao.getRecordsBetweenDates(dates.first(), dates.last())
    }

    /**
     * 获取今天使用时长最长的APP
     */
    suspend fun getMostUsedAppToday(): AppUsageEntity? {
        return dao.getMostUsedAppToday(getTodayDate())
    }
}
