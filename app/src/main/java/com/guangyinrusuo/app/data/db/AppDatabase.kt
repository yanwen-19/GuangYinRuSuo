/*
 * 光阴如梭 - 个人效率工具
 * AppDatabase.kt
 *
 * Room 数据库核心类
 * 采用单例模式确保全局只有一个数据库实例
 *
 * 版本管理：
 * 当前版本 = 1
 * 后续需要改表结构时，需要：
 * 1. 递增 version
 * 2. 提供 Migration 迁移方案（或使用 fallbackToDestructiveMigration 开发阶段用）
 */

package com.guangyinrusuo.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.guangyinrusuo.app.data.db.dao.AppUsageDao
import com.guangyinrusuo.app.data.db.dao.PomoSessionDao
import com.guangyinrusuo.app.data.db.dao.TaskDao
import com.guangyinrusuo.app.data.db.entity.AppUsageEntity
import com.guangyinrusuo.app.data.db.entity.PomoSessionEntity
import com.guangyinrusuo.app.data.db.entity.TaskEntity

/**
 * Room 数据库
 * entities = 列出所有实体类，Room会为它们创建表
 * version = 数据库版本号，用于迁移
 *
 * Room 会在编译时通过 KSP 自动生成实现类 AppDatabase_Impl
 */
@Database(
    entities = [
        TaskEntity::class,
        PomoSessionEntity::class,
        AppUsageEntity::class
    ],
    version = 1,
    exportSchema = false  // 开发阶段设为 false，正式发布建议设为 true 以追踪变更历史
)
abstract class AppDatabase : RoomDatabase() {

    // 抽象方法 — Room自动生成DAO实现
    abstract fun taskDao(): TaskDao
    abstract fun pomoSessionDao(): PomoSessionDao
    abstract fun appUsageDao(): AppUsageDao

    companion object {
        // 数据库文件名
        private const val DATABASE_NAME = "guangyinrusuo.db"

        // 双重校验锁的单例模式
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 获取数据库实例
         * 如果实例不存在则创建，存在则直接返回
         *
         * @param context ApplicationContext（防止内存泄漏，不使用Activity context）
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    // 开发阶段允许在主线程查询（仅调试，正式版应移除）
                    // .allowMainThreadQueries()
                    // 版本升级时若没有 migration 则销毁重建（开发阶段方便，发布前应替换为正式 migration）
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
