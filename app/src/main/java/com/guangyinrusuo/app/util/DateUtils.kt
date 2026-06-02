/*
 * 光阴如梭 - 个人效率工具
 * DateUtils.kt
 *
 * 日期时间工具类
 * 提供常用日期格式化、获取日/周/月边界等方法
 */

package com.guangyinrusuo.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 日期工具类
 */
object DateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    private val weekDayFormat = SimpleDateFormat("E", Locale.getDefault())

    /**
     * 获取今天的日期字符串 (yyyy-MM-dd)
     */
    fun getTodayDate(): String = dateFormat.format(Date())

    /**
     * 获取今天的开始时间戳 (毫秒, 00:00:00.000)
     */
    fun getTodayStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * 获取今天的结束时间戳 (毫秒, 23:59:59.999)
     */
    fun getTodayEnd(): Long {
        return getTodayStart() + 24 * 60 * 60 * 1000 - 1
    }

    /**
     * 获取一周前的开始时间戳
     */
    fun getWeekAgoStart(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * 格式化时间戳为可读时间 (HH:mm)
     */
    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    /**
     * 格式化时间戳为日期时间 (MM-dd HH:mm)
     */
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    /**
     * 格式化毫秒为时长字符串
     */
    fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%d:%02d", minutes, seconds)
            else -> String.format("0:%02d", seconds)
        }
    }

    /**
     * 获取星期几 (如 "周一")
     */
    fun getWeekDay(timestamp: Long): String {
        return weekDayFormat.format(Date(timestamp))
    }
}
