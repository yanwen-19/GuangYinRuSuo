/*
 * 光阴如梭 - 个人效率工具
 * PomoSessionEntity.kt
 *
 * 番茄钟专注记录实体
 * 对应数据库中的 pomo_sessions 表
 *
 * 每次番茄钟完成（专注或休息结束）都会在此表中插入一条记录
 * 用于「统计」模块中的日/周专注时长分析和时间线回放
 *
 * 字段说明：
 * - id: 自动递增主键
 * - startTime: 开始时间戳 (毫秒)
 * - endTime: 结束时间戳 (毫秒, 可能为空=未完成)
 * - durationSeconds: 实际持续秒数 (排除暂停时间)
 * - type: 类型 — "focus" 专注 / "break" 休息
 * - taskId: 关联的待办事项ID (可选, 番茄钟可绑定某个任务)
 */

package com.guangyinrusuo.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 番茄钟会话记录实体
 *
 * @ForeignKey 定义外键约束：taskId 引用 tasks 表的 id
 *   当关联的任务被删除时，此记录的 taskId 自动置为 NULL (SET NULL)
 * @Index 为 taskId 和 startTime 创建索引，加速按任务和按时间的查询
 */
@Entity(
    tableName = "pomo_sessions",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.SET_NULL  // 任务删除后不删除记录，仅断开关联
        )
    ],
    indices = [
        Index("task_id"),
        Index("start_time")
    ]
)
data class PomoSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "start_time")
    val startTime: Long,  // 毫秒时间戳

    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,  // 毫秒时间戳，null = 未完成

    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int,  // 实际专注/休息秒数

    @ColumnInfo(name = "type")
    val type: String,  // "focus" 或 "break"

    @ColumnInfo(name = "task_id")
    val taskId: Long? = null  // 关联的待办事项ID
)
