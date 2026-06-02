/*
 * 光阴如梭 - 个人效率工具
 * AppUsageDao.kt
 *
 * APP使用记录数据访问对象
 * 定义对 app_usage_records 表的所有操作
 *
 * WorkManager 定时写入使用数据，UI层读取用于统计图表显示
 * 使用 INSERT ... ON CONFLICT REPLACE 实现 upsert 语义
 */

package com.guangyinrusuo.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guangyinrusuo.app.data.db.entity.AppUsageEntity

/**
 * APP使用记录DAO
 */
@Dao
interface AppUsageDao {

    /**
     * 获取指定日期的所有APP使用记录
     */
    @Query("SELECT * FROM app_usage_records WHERE date = :date ORDER BY total_time_in_foreground DESC")
    suspend fun getRecordsByDate(date: String): List<AppUsageEntity>

    /**
     * 获取指定日期范围内的记录（用于周统计）
     */
    @Query("""
        SELECT * FROM app_usage_records
        WHERE date >= :startDate AND date <= :endDate
        ORDER BY date ASC, total_time_in_foreground DESC
    """)
    suspend fun getRecordsBetweenDates(startDate: String, endDate: String): List<AppUsageEntity>

    /**
     * 插入或替换记录（upsert）
     * 同一天同一APP的数据会覆盖更新
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: AppUsageEntity)

    /**
     * 批量插入 （WorkManager 批量写入用）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AppUsageEntity>)

    /**
     * 获取今天总使用时长最多的APP
     */
    @Query("""
        SELECT * FROM app_usage_records
        WHERE date = :date
        ORDER BY total_time_in_foreground DESC
        LIMIT 1
    """)
    suspend fun getMostUsedAppToday(date: String): AppUsageEntity?

    /**
     * 获取某个应用的今日使用时长
     */
    @Query("""
        SELECT total_time_in_foreground
        FROM app_usage_records
        WHERE package_name = :packageName AND date = :date
        LIMIT 1
    """)
    suspend fun getAppUsageToday(packageName: String, date: String): Long?

    /**
     * 删除30天前的历史记录
     */
    @Query("DELETE FROM app_usage_records WHERE date < :cutoffDate")
    suspend fun deleteOldRecords(cutoffDate: String)

    /**
     * 获取今天所有记录的总前台时长
     */
    @Query("""
        SELECT COALESCE(SUM(total_time_in_foreground), 0)
        FROM app_usage_records
        WHERE date = :date
    """)
    suspend fun getTodayTotalUsage(date: String): Long
}
