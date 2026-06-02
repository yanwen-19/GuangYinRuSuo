/*
 * 光阴如梭 - 个人效率工具
 * AppUsageEntity.kt
 *
 * APP使用记录实体
 * 对应数据库中的 app_usage_records 表
 *
 * 由 WorkManager 定时任务 (AppUsageMonitorWorker) 周期性写入
 * 记录每个APP的每日使用时长，用于：
 * 1. 统计模块的「APP使用时间分布」图表
 * 2. 娱乐监控模块判断是否超时
 *
 * 字段说明：
 * - id: 自动递增主键
 * - packageName: 应用包名 (如 com.zhihu.android)
 * - appName: 应用显示名称 (如 知乎)
 * - totalTimeInForeground: 今日前台使用总时长 (毫秒)
 * - date: 记录日期 (yyyy-MM-dd 格式)
 * - lastChecked: 最后检查时间戳 — 用于增量更新
 */

package com.guangyinrusuo.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * APP使用记录实体
 *
 * 设计要点：
 * 同一应用同一天只保留一条记录，通过 packageName + date 联合唯一约束保证
 * WorkManager 每次检查时更新 totalTimeInForeground 和 lastChecked
 */
@Entity(
    tableName = "app_usage_records",
    indices = [
        Index(value = ["package_name", "date"], unique = true)  // 同一天同一APP只一条
    ]
)
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "package_name")
    val packageName: String,  // 应用包名

    @ColumnInfo(name = "app_name")
    val appName: String,  // 应用显示名称

    @ColumnInfo(name = "total_time_in_foreground")
    val totalTimeInForeground: Long = 0L,  // 毫秒

    @ColumnInfo(name = "date")
    val date: String,  // yyyy-MM-dd

    @ColumnInfo(name = "last_checked")
    val lastChecked: Long = System.currentTimeMillis()
)
