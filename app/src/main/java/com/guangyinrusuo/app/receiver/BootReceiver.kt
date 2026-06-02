/*
 * 光阴如梭 - 个人效率工具
 * BootReceiver.kt
 *
 * 开机自启动广播接收器
 * 手机重启后重新注册 WorkManager 的 APP监控定时任务
 *
 * 澎湃OS适配：
 * 需要用户手动开启「自启动」权限
 * 设置路径：设置 → 应用设置 → 应用管理 → 光阴如梭 → 自启动 → 开启
 */

package com.guangyinrusuo.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.guangyinrusuo.app.util.AppUsageMonitorWorker
import java.util.concurrent.TimeUnit

/**
 * 开机自启动广播接收
 *
 * 注意：Android 14+ 对隐式广播限制更严格，
 * 但 BOOT_COMPLETED 属于例外，仍然可以接收
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // 重新注册APP监控定时任务
            scheduleAppUsageMonitoring(context)
        }
    }

    /**
     * 注册 APP 使用时长监控定时任务
     * 每15分钟执行一次
     */
    private fun scheduleAppUsageMonitoring(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<AppUsageMonitorWorker>(
            15, TimeUnit.MINUTES
        )
            .addTag("app_usage_monitor")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "app_usage_monitor",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
