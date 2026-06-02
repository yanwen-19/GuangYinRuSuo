/*
 * 光阴如梭 - 个人效率工具
 * PreferenceManager.kt
 *
 * SharedPreferences 封装管理类
 * 用于存储不需要持久化到数据库的轻量配置数据
 *
 * 存储内容：
 * - 番茄钟配置（时长、休息时长等）
 * - 娱乐APP限额列表及限额
 * - 首次使用标记
 * - 应用状态
 */

package com.guangyinrusuo.app.data.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences管理器
 * 所有键值定义在 companion object 中统一管理，避免字符串散落各处
 */
class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    // ========== 番茄钟设置 ==========

    /**
     * 专注时长（分钟），默认25分钟
     */
    var focusDurationMinutes: Int
        get() = prefs.getInt(KEY_FOCUS_DURATION, 25)
        set(value) = prefs.edit().putInt(KEY_FOCUS_DURATION, value).apply()

    /**
     * 短休息时长（分钟），默认5分钟
     */
    var shortBreakMinutes: Int
        get() = prefs.getInt(KEY_SHORT_BREAK, 5)
        set(value) = prefs.edit().putInt(KEY_SHORT_BREAK, value).apply()

    /**
     * 长休息时长（分钟），默认15分钟
     */
    var longBreakMinutes: Int
        get() = prefs.getInt(KEY_LONG_BREAK, 15)
        set(value) = prefs.edit().putInt(KEY_LONG_BREAK, value).apply()

    /**
     * 长休息间隔（第几个番茄钟后长休息），默认4个
     */
    var longBreakInterval: Int
        get() = prefs.getInt(KEY_LONG_BREAK_INTERVAL, 4)
        set(value) = prefs.edit().putInt(KEY_LONG_BREAK_INTERVAL, value).apply()

    // ========== 番茄钟运行时状态 ==========

    /**
     * 当前番茄钟会话ID（用于恢复）
     */
    var currentSessionId: Long?
        get() = prefs.getLong(KEY_CURRENT_SESSION_ID, -1L).let { if (it == -1L) null else it }
        set(value) = prefs.edit().putLong(KEY_CURRENT_SESSION_ID, value ?: -1L).apply()

    /**
     * 番茄钟剩余秒数（用于Activity重建恢复）
     */
    var remainingSeconds: Int
        get() = prefs.getInt(KEY_REMAINING_SECONDS, 0)
        set(value) = prefs.edit().putInt(KEY_REMAINING_SECONDS, value).apply()

    /**
     * 番茄钟当前状态
     * "idle" | "focusing" | "break"
     */
    var pomodoroState: String
        get() = prefs.getString(KEY_POMODORO_STATE, "idle") ?: "idle"
        set(value) = prefs.edit().putString(KEY_POMODORO_STATE, value).apply()

    /**
     * 当前连续完成番茄钟数
     */
    var completedPomosInRow: Int
        get() = prefs.getInt(KEY_COMPLETED_POMOS, 0)
        set(value) = prefs.edit().putInt(KEY_COMPLETED_POMOS, value).apply()

    // ========== 娱乐监控配置 ==========

    /**
     * 获取被监控应用的限额（分钟）
     * key = packageName, value = 每日限额分钟数
     */
    fun getMonitoredAppLimit(packageName: String): Int {
        return prefs.getInt(KEY_MONITOR_PREFIX + packageName, 30)  // 默认30分钟
    }

    /**
     * 设置被监控应用的限额
     */
    fun setMonitoredAppLimit(packageName: String, limitMinutes: Int) {
        prefs.edit().putInt(KEY_MONITOR_PREFIX + packageName, limitMinutes).apply()
    }

    /**
     * 获取所有被监控应用的包名列表（简单实现，存储为逗号分隔字符串）
     * 更完善的实现可以单独建表，但当前需求用SharedPreferences足够
     */
    fun getMonitoredPackages(): Set<String> {
        return prefs.getStringSet(KEY_MONITORED_PACKAGES, emptySet()) ?: emptySet()
    }

    /**
     * 添加被监控应用
     */
    fun addMonitoredPackage(packageName: String) {
        val set = getMonitoredPackages().toMutableSet()
        set.add(packageName)
        prefs.edit().putStringSet(KEY_MONITORED_PACKAGES, set).apply()
    }

    /**
     * 移除被监控应用
     */
    fun removeMonitoredPackage(packageName: String) {
        val set = getMonitoredPackages().toMutableSet()
        set.remove(packageName)
        prefs.edit().putStringSet(KEY_MONITORED_PACKAGES, set).apply()
    }

    // ========== 应用状态 ==========

    /**
     * 是否首次启动
     */
    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    /**
     * 统计页面「今日专注总秒数」缓存
     */
    var todayFocusSecondsCache: Int
        get() = prefs.getInt(KEY_TODAY_FOCUS_CACHE, 0)
        set(value) = prefs.edit().putInt(KEY_TODAY_FOCUS_CACHE, value).apply()

    /**
     * 缓存日期（用于判断今日缓存是否过期）
     */
    var cacheDate: String
        get() = prefs.getString(KEY_CACHE_DATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CACHE_DATE, value).apply()

    companion object {
        private const val PREF_FILE_NAME = "guangyinrusuo_prefs"

        // 番茄钟设置键
        private const val KEY_FOCUS_DURATION = "focus_duration"
        private const val KEY_SHORT_BREAK = "short_break"
        private const val KEY_LONG_BREAK = "long_break"
        private const val KEY_LONG_BREAK_INTERVAL = "long_break_interval"

        // 番茄钟运行状态键
        private const val KEY_CURRENT_SESSION_ID = "current_session_id"
        private const val KEY_REMAINING_SECONDS = "remaining_seconds"
        private const val KEY_POMODORO_STATE = "pomodoro_state"
        private const val KEY_COMPLETED_POMOS = "completed_pomos"

        // 娱乐监控键
        private const val KEY_MONITOR_PREFIX = "monitor_limit_"
        private const val KEY_MONITORED_PACKAGES = "monitored_packages"

        // 应用状态键
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_TODAY_FOCUS_CACHE = "today_focus_cache"
        private const val KEY_CACHE_DATE = "cache_date"
    }
}
