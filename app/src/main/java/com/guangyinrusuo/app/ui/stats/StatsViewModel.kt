/*
 * 光阴如梭 - 个人效率工具
 * StatsViewModel.kt
 *
 * 统计页面 ViewModel
 * 管理专注记录和APP使用数据的加载与展示
 *
 * 数据来源：
 * - 专注记录：PomoRepository (从Room读取)
 * - APP使用：UsageRepository (从Room读取)
 */

package com.guangyinrusuo.app.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.guangyinrusuo.app.GuangYinRuSuoApplication
import kotlinx.coroutines.launch

/**
 * 统计ViewModel
 *
 * 提供两个Tab的数据：
 * 1. 专注记录：日/周专注时长柱状图
 * 2. APP使用：各应用使用时间饼图
 */
class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GuangYinRuSuoApplication
    private val pomoRepository = app.pomoRepository
    private val usageRepository = app.usageRepository

    // 本周每日专注数据 (日期标签, 秒数)
    private val _weeklyFocusData = MutableLiveData<List<Pair<String, Int>>>(emptyList())
    val weeklyFocusData: LiveData<List<Pair<String, Int>>> = _weeklyFocusData

    // 今日专注总秒数
    private val _todayFocusSeconds = MutableLiveData(0)
    val todayFocusSeconds: LiveData<Int> = _todayFocusSeconds

    // 今日完成番茄钟数
    private val _todaySessions = MutableLiveData(0)
    val todaySessions: LiveData<Int> = _todaySessions

    // APP使用列表 (按使用时长降序)
    private val _appUsageList = MutableLiveData<List<AppUsageItem>>(emptyList())
    val appUsageList: LiveData<List<AppUsageItem>> = _appUsageList

    // 加载状态
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * APP使用数据项（UI展示用）
     */
    data class AppUsageItem(
        val appName: String,
        val packageName: String,
        val usageMillis: Long,
        val percentage: Float  // 占今天总时长的百分比
    )

    /**
     * 加载所有统计数据
     */
    fun loadAllStats() {
        loadFocusStats()
        loadAppUsageStats()
    }

    /**
     * 加载专注记录统计
     */
    fun loadFocusStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 加载周数据
                val weeklyData = pomoRepository.getWeeklyFocusData()
                _weeklyFocusData.value = weeklyData

                // 加载今日数据
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val dayStart = calendar.timeInMillis
                val dayEnd = dayStart + 24 * 60 * 60 * 1000

                _todayFocusSeconds.value = pomoRepository.getTotalFocusSeconds(dayStart, dayEnd)
                _todaySessions.value = pomoRepository.getFocusSessionCount(dayStart, dayEnd)
            } catch (e: Exception) {
                // 静默失败 — UI保持上次数据
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载APP使用统计
     */
    fun loadAppUsageStats() {
        viewModelScope.launch {
            try {
                val records = usageRepository.getTodayUsageList()
                val totalUsage = records.sumOf { it.totalTimeInForeground }

                val items = records.map { record ->
                    AppUsageItem(
                        appName = record.appName,
                        packageName = record.packageName,
                        usageMillis = record.totalTimeInForeground,
                        percentage = if (totalUsage > 0) {
                            record.totalTimeInForeground.toFloat() / totalUsage * 100
                        } else 0f
                    )
                }.sortedByDescending { it.usageMillis }

                _appUsageList.value = items
            } catch (e: Exception) {
                // 静默失败
            }
        }
    }

    /**
     * 格式化时长（毫秒 → 可读字符串）
     */
    fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return when {
            hours > 0 -> "${hours}小时${minutes}分钟"
            minutes > 0 -> "${minutes}分钟"
            else -> "${totalSeconds}秒"
        }
    }

    /**
     * 格式化专注秒数为可读字符串
     */
    fun formatFocusSeconds(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return when {
            hours > 0 -> "${hours}h${minutes}m"
            minutes > 0 -> "${minutes}分钟"
            else -> "${seconds}秒"
        }
    }
}
