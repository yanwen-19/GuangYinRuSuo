/*
 * 光阴如梭 - 个人效率工具
 * AppUsageMonitorWorker.kt
 *
 * 娱乐APP使用时长监控 Worker (WorkManager)
 *
 * 工作流程：
 * 1. WorkManager 周期性启动（默认每15分钟）
 * 2. 查询 UsageStatsManager 获取各应用使用时长
 * 3. 与用户设定的限额对比
 * 4. 超时 → 发送通知 → 启动 FullscreenAlertActivity
 *
 * 澎湃OS适配要点：
 * - UsageStatsManager 是系统级API，不需要额外权限声明（但需用户手动开启权限）
 * - 权限路径：设置 → 应用设置 → 权限管理 → 特殊权限 → 使用情况访问权限
 * - 如果用户未开启此权限，queryUsageStats 将返回空列表
 *
 * 限制：
 * - UsageStatsManager 只能在 5分钟 的时间窗口内查询
 * - 查询时间间隔建议 >= 5分钟
 * - 本 Worker 设置为 15分钟 执行一次，符合系统建议
 */

package com.guangyinrusuo.app.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.guangyinrusuo.app.GuangYinRuSuoApplication
import com.guangyinrusuo.app.R
import com.guangyinrusuo.app.data.db.entity.AppUsageEntity
import com.guangyinrusuo.app.data.preferences.PreferenceManager
import com.guangyinrusuo.app.ui.FullscreenAlertActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * APP使用时长监控 Worker
 *
 * 继承 CoroutineWorker 以使用协程（非阻塞式数据库操作）
 */
class AppUsageMonitorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val app = applicationContext as GuangYinRuSuoApplication
    private val prefs = PreferenceManager(applicationContext)
    private val usageRepository = app.usageRepository

    override suspend fun doWork(): Result {
        return try {
            // 检查 UsageStats 权限
            if (!hasUsageStatsPermission()) {
                return Result.success() // 无权限则跳过
            }

            // 获取 UsageStatsManager
            val usageStatsManager = applicationContext.getSystemService(
                Context.USAGE_STATS_SERVICE
            ) as UsageStatsManager

            // 查询最近24小时的应用使用数据
            val endTime = System.currentTimeMillis()
            val startTime = endTime - 24 * 60 * 60 * 1000

            val usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            if (usageStatsList.isNullOrEmpty()) {
                return Result.success()
            }

            val today = dateFormat.format(Date())
            val monitoredPackages = prefs.getMonitoredPackages()

            // 遍历并更新数据库
            for (stats in usageStatsList) {
                val packageName = stats.packageName ?: continue

                // 只记录被监控的APP（但也可以统计所有APP用于图表展示）
                // 为方便统计，所有APP都记录
                val appName = getAppName(packageName)
                val usageMillis = stats.totalTimeInForeground

                if (usageMillis <= 0) continue

                // 写入数据库
                val entity = AppUsageEntity(
                    packageName = packageName,
                    appName = appName,
                    totalTimeInForeground = usageMillis,
                    date = today
                )
                usageRepository.saveRecord(entity)

                // 检查是否超时（仅检查被监控的应用）
                if (packageName in monitoredPackages) {
                    val limitMinutes = prefs.getMonitoredAppLimit(packageName)
                    val limitMillis = limitMinutes * 60 * 1000L

                    if (usageMillis > limitMillis) {
                        // 超时！发送通知
                        sendOverLimitNotification(appName, packageName, limitMinutes)
                    }
                }
            }

            Result.success()
        } catch (e: SecurityException) {
            // 权限被拒绝
            Result.success()
        } catch (e: Exception) {
            // 其他错误，下次重试
            Result.retry()
        }
    }

    /**
     * 获取应用的显示名称
     */
    private fun getAppName(packageName: String): String {
        return try {
            val packageManager = applicationContext.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName // 获取失败则使用包名
        }
    }

    /**
     * 发送超时通知
     * 点击通知跳转到 FullscreenAlertActivity
     */
    private fun sendOverLimitNotification(appName: String, packageName: String, limitMinutes: Int) {
        // 构建 Intent → FullscreenAlertActivity
        val intent = Intent(applicationContext, FullscreenAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(FullscreenAlertActivity.EXTRA_APP_NAME, appName)
            putExtra(FullscreenAlertActivity.EXTRA_PACKAGE_NAME, packageName)
            putExtra(FullscreenAlertActivity.EXTRA_LIMIT_MINUTES, limitMinutes)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            packageName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            GuangYinRuSuoApplication.CHANNEL_APP_MONITOR
        )
            .setSmallIcon(R.drawable.ic_stop)
            .setContentTitle("${appName} 使用超时")
            .setContentText("已超过每日限额 ${limitMinutes} 分钟，该休息了！")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(packageName.hashCode(), notification)
    }

    /**
     * 检查 UsageStats 权限
     */
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = applicationContext.getSystemService(Context.APP_OPS_SERVICE)
                as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            applicationContext.packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }
}
