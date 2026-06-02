/*
 * 光阴如梭 - 个人效率工具
 * TaskEntity.kt
 *
 * 待办事项 (Task) 数据实体
 * 对应数据库中的 tasks 表
 *
 * 字段说明：
 * - id: 自动递增主键
 * - title: 待办标题 (必填)
 * - description: 备注说明 (可选)
 * - dueDate: 截止时间 (毫秒时间戳, 可选)
 * - isCompleted: 是否已完成
 * - createdAt: 创建时间 (毫秒时间戳)
 * - completedAt: 完成时间 (毫秒时间戳, 可选 — 用于时间线回放功能)
 * - sortOrder: 排序权重 (数字越小越靠前)
 */

package com.guangyinrusuo.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 待办事项实体类
 *
 * Room 数据库使用注解来定义表结构和字段映射：
 * @Entity 标记这是一个数据库表，tableName 指定表名
 * @PrimaryKey 标记主键，autoGenerate 表示自动递增
 * @ColumnInfo 指定字段名（不指定则使用变量名）
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0
)
