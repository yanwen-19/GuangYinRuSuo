/*
 * 光阴如梭 - 个人效率工具
 * PomodoroViewModel.kt
 *
 * 番茄钟模块 ViewModel
 * 核心计时引擎：管理专注/休息状态切换，与前台服务通信
 *
 * 状态流转：
 * IDLE → FOCUSING → (倒计时结束→记录完成) → BREAK → (结束) → IDLE
 *         ↕ 暂停 ↕
 * 暂停时保留剩余时间，恢复时继续倒计时
 */

package com.guangyinrusuo.app.ui.pomodoro

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.guangyinrusuo.app.GuangYinRuSuoApplication
import com.guangyinrusuo.app.data.db.entity.TaskEntity
import com.guangyinrusuo.app.data.preferences.PreferenceManager
import com.guangyinrusuo.app.service.PomoForegroundService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 番茄钟状态枚举
 */
enum class PomoState {
    IDLE,       // 空闲 — 未开始
    FOCUSING,   // 专注中
    PAUSED,     // 暂停
    BREAK       // 休息中
}

/**
 * 番茄钟 ViewModel
 *
 * 核心计时逻辑：
 * - 使用协程延迟实现倒计时（每秒 tick 一次）
 * - 时间数据持久化到 SharedPreferences（防止进程被杀丢失状态）
 * - 通过 LiveData 向 Fragment 暴露当前状态和剩余时间
 * - 通过 ForegroundService 实现后台保活
 */
class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GuangYinRuSuoApplication
    private val prefs: PreferenceManager = app.preferenceManager
    private val pomoRepository = app.pomoRepository
    private val taskRepository = app.taskRepository

    // ===== 状态 LiveData =====

    private val _state = MutableLiveData(PomoState.IDLE)
    val state: LiveData<PomoState> = _state

    // 剩余秒数
    private val _remainingSeconds = MutableLiveData(0)
    val remainingSeconds: LiveData<Int> = _remainingSeconds

    // 总秒数 (用于进度计算)
    private val _totalSeconds = MutableLiveData(0)
    val totalSeconds: LiveData<Int> = _totalSeconds

    // 今日专注总秒数
    private val _todayFocusSeconds = MutableLiveData(0)
    val todayFocusSeconds: LiveData<Int> = _todayFocusSeconds

    // 今日完成番茄钟数
    private val _todaySessionCount = MutableLiveData(0)
    val todaySessionCount: LiveData<Int> = _todaySessionCount

    // 当前关联的任务
    private val _selectedTask = MutableLiveData<TaskEntity?>()
    val selectedTask: LiveData<TaskEntity?> = _selectedTask

    // 当前会话ID (用于记录到数据库)
    private var currentSessionId: Long? = null

    // 倒计时协程 Job
    private var timerJob: Job? = null

    // 当前专注模式下已消耗的总秒数（用于判断是否完成了一个完整周期）
    private var elapsedSeconds = 0

    init {
        // 恢复状态（如App被系统杀后重建）
        restoreState()
        // 加载今日统计
        loadTodayStats()
    }

    /**
     * 从 SharedPreferences 恢复上一次的番茄钟状态
     * 处理场景：用户离开番茄钟页面、App被系统回收
     */
    private fun restoreState() {
        val savedState = prefs.pomodoroState
        val savedRemaining = prefs.remainingSeconds
        val savedSessionId = prefs.currentSessionId

        when (savedState) {
            "focusing" -> {
                if (savedRemaining > 0) {
                    _remainingSeconds.value = savedRemaining
                    _state.value = PomoState.FOCUSING
                    currentSessionId = savedSessionId
                    // 恢复前台服务
                    startForegroundService()
                }
            }
            "break" -> {
                if (savedRemaining > 0) {
                    _remainingSeconds.value = savedRemaining
                    _state.value = PomoState.BREAK
                }
            }
            "paused" -> {
                if (savedRemaining > 0) {
                    _remainingSeconds.value = savedRemaining
                    _state.value = PomoState.PAUSED
                    currentSessionId = savedSessionId
                }
            }
        }
    }

    /**
     * 加载今日专注总时长和番茄钟数量
     */
    fun loadTodayStats() {
        viewModelScope.launch {
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val dayStart = calendar.timeInMillis
            val dayEnd = dayStart + 24 * 60 * 60 * 1000

            _todayFocusSeconds.value = pomoRepository.getTotalFocusSeconds(dayStart, dayEnd)
            _todaySessionCount.value = pomoRepository.getFocusSessionCount(dayStart, dayEnd)
        }
    }

    /**
     * 开始专注
     */
    fun startFocus(taskId: Long? = null) {
        if (_state.value != PomoState.IDLE && _state.value != PomoState.PAUSED) return

        viewModelScope.launch {
            // 获取专注时长（分钟→秒）
            val focusMinutes = prefs.focusDurationMinutes
            val totalSec = focusMinutes * 60

            _totalSeconds.value = totalSec
            _remainingSeconds.value = totalSec
            elapsedSeconds = 0
            _state.value = PomoState.FOCUSING

            // 在数据库中创建一条新记录
            currentSessionId = pomoRepository.startSession("focus", taskId)
            prefs.currentSessionId = currentSessionId
            prefs.pomodoroState = "focusing"

            // 启动前台服务
            startForegroundService()

            // 开始倒计时
            startTimer(totalSec)
        }
    }

    /**
     * 暂停专注
     */
    fun pauseFocus() {
        if (_state.value != PomoState.FOCUSING) return
        _state.value = PomoState.PAUSED
        prefs.pomodoroState = "paused"

        // 保存剩余时间
        _remainingSeconds.value?.let { prefs.remainingSeconds = it }

        // 停止协程倒计时
        timerJob?.cancel()

        // 更新前台服务状态
        stopForegroundService()
    }

    /**
     * 恢复专注
     */
    fun resumeFocus() {
        if (_state.value != PomoState.PAUSED) return
        _state.value = PomoState.FOCUSING
        prefs.pomodoroState = "focusing"

        // 重新启动前台服务
        startForegroundService()

        // 继续倒计时
        _remainingSeconds.value?.let { totalSec ->
            startTimer(totalSec)
        }
    }

    /**
     * 停止专注（用户主动停止）
     */
    fun stopFocus() {
        timerJob?.cancel()
        timerJob = null

        // 记录未完成的会话（不保存到数据库）
        currentSessionId?.let { id ->
            viewModelScope.launch {
                val elapsed = _totalSeconds.value?.minus(_remainingSeconds.value ?: 0) ?: 0
                pomoRepository.finishSession(id, elapsed)
            }
        }

        resetToIdle()
    }

    /**
     * 开始休息
     */
    private fun startBreak() {
        val breakMinutes = prefs.shortBreakMinutes
        val totalSec = breakMinutes * 60

        _totalSeconds.value = totalSec
        _remainingSeconds.value = totalSec
        elapsedSeconds = 0
        _state.value = PomoState.BREAK
        prefs.pomodoroState = "break"

        viewModelScope.launch {
            // 插入休息记录
            pomoRepository.startSession("break")
        }

        // 开始休息倒计时
        startTimer(totalSec)
    }

    /**
     * 核心倒计时引擎
     * 使用协程每秒 tick 一次
     */
    private fun startTimer(totalSeconds: Int) {
        // 取消之前的计时任务
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (isActive && (_remainingSeconds.value ?: 0) > 0) {
                delay(1000) // 每秒减1
                val remaining = (_remainingSeconds.value ?: 1) - 1
                _remainingSeconds.value = remaining
                elapsedSeconds++

                // 实时持久化剩余秒数（防止进程被杀）
                if (remaining % 5 == 0) {  // 每5秒写一次SharedPreferences，减少IO次数
                    prefs.remainingSeconds = remaining
                }

                // 倒计时结束
                if (remaining <= 0) {
                    onTimerComplete()
                    return@launch
                }
            }
        }
    }

    /**
     * 倒计时结束回调
     */
    private fun onTimerComplete() {
        timerJob = null

        viewModelScope.launch {
            when (_state.value) {
                PomoState.FOCUSING -> {
                    // 专注结束：记录完成 + 更新统计
                    currentSessionId?.let { id ->
                        pomoRepository.finishSession(id, _totalSeconds.value ?: 1500)
                    }
                    prefs.completedPomosInRow = prefs.completedPomosInRow + 1
                    loadTodayStats()

                    // 自动切换到休息
                    // 判断是否需要长休息
                    if (prefs.completedPomosInRow % prefs.longBreakInterval == 0) {
                        // 长休息
                        val longBreakSec = prefs.longBreakMinutes * 60
                        _totalSeconds.value = longBreakSec
                        _remainingSeconds.value = longBreakSec
                    } else {
                        // 短休息
                        val shortBreakSec = prefs.shortBreakMinutes * 60
                        _totalSeconds.value = shortBreakSec
                        _remainingSeconds.value = shortBreakSec
                    }
                    _state.value = PomoState.BREAK
                    prefs.pomodoroState = "break"
                    elapsedSeconds = 0
                    startTimer(_remainingSeconds.value ?: 300)
                }
                PomoState.BREAK -> {
                    // 休息结束，回到空闲
                    resetToIdle()
                }
                else -> resetToIdle()
            }
        }
    }

    /**
     * 重置为空闲状态
     */
    private fun resetToIdle() {
        timerJob?.cancel()
        timerJob = null
        _state.value = PomoState.IDLE
        _remainingSeconds.value = 0
        _totalSeconds.value = 0
        currentSessionId = null
        elapsedSeconds = 0
        prefs.pomodoroState = "idle"
        prefs.currentSessionId = null
        prefs.remainingSeconds = 0

        stopForegroundService()
    }

    /**
     * 启动前台服务（系统几乎不杀前台服务）
     */
    private fun startForegroundService() {
        val intent = Intent(app, PomoForegroundService::class.java).apply {
            putExtra(PomoForegroundService.EXTRA_REMAINING_SECONDS, _remainingSeconds.value ?: 0)
            putExtra(PomoForegroundService.EXTRA_STATE, _state.value?.name ?: "FOCUSING")
        }
        app.startForegroundService(intent)
    }

    /**
     * 停止前台服务
     */
    private fun stopForegroundService() {
        val intent = Intent(app, PomoForegroundService::class.java)
        app.stopService(intent)
    }

    /**
     * 获取所有未完成的待办（用于关联任务选择器）
     */
    fun loadUncompletedTasks(callback: (List<TaskEntity>) -> Unit) {
        viewModelScope.launch {
            taskRepository.uncompletedTasks.collect { tasks ->
                callback(tasks)
            }
        }
    }

    /**
     * 格式化剩余时间为 MM:SS
     */
    fun getFormattedTime(seconds: Int): String {
        val min = seconds / 60
        val sec = seconds % 60
        return String.format("%02d:%02d", min, sec)
    }

    /**
     * 获取进度百分比 (0-100)
     */
    fun getProgress(): Int {
        val total = _totalSeconds.value ?: 1
        val remaining = _remainingSeconds.value ?: 0
        return if (total > 0) ((total - remaining) * 100 / total) else 0
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
