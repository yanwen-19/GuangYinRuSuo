/*
 * 光阴如梭 - 个人效率工具
 * PermissionUtils.kt
 *
 * 权限工具类
 * 封装澎湃OS/Android常用权限的检查和跳转
 *
 * 包含：
 * 1. UsageStats 权限检查与跳转
 * 2. 通知权限检查 (Android 13+)
 * 3. 电池优化白名单检查与跳转
 * 4. 自启动引导（仅提示，无法代码判断）
 */

package com.guangyinrusuo.app.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * 权限工具类
 */
object PermissionUtils {

    /**
     * 检查通知权限 (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 13 以下默认有通知权限
        }
    }

    /**
     * 检查 UsageStats 权限
     * 这是监控APP使用时长必需的权限
     * 需要用户手动在系统设置中开启
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE)
                    as android.app.AppOpsManager
            val mode = appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
            return mode == android.app.AppOpsManager.MODE_ALLOWED
        }
        return true
    }

    /**
     * 跳转到 UsageStats 权限设置页
     * 引导用户手动开启
     */
    fun openUsageStatsSettings(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * 检查是否已加入电池优化白名单
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE)
                    as android.os.PowerManager
            return powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
        return true
    }

    /**
     * 请求加入电池优化白名单
     */
    fun requestIgnoreBatteryOptimizations(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * 引导用户开启自启动（MIUI/澎湃OS专用）
     * 注意：自启动权限没有标准的Android API，
     * 只能引导用户手动操作
     *
     * 引导文字：
     * "请前往 设置 → 应用设置 → 应用管理 → 光阴如梭 → 自启动 → 开启"
     */
    fun getAutoStartGuideText(): String {
        return "请前往：设置 → 应用设置 → 应用管理 → 光阴如梭 → 自启动 → 开启"
    }

    /**
     * 打开系统应用详情页（方便用户手动授权）
     */
    fun openAppDetailsSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
