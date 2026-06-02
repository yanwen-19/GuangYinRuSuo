package com.guangyinrusuo.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.guangyinrusuo.app.data.db.AppDatabase
import com.guangyinrusuo.app.data.preferences.PreferenceManager
import com.guangyinrusuo.app.data.repository.PomoRepository
import com.guangyinrusuo.app.data.repository.TaskRepository
import com.guangyinrusuo.app.data.repository.UsageRepository
import com.guangyinrusuo.app.util.AppUsageMonitorWorker
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GuangYinRuSuoApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val preferenceManager: PreferenceManager by lazy { PreferenceManager(this) }
    val taskRepository: TaskRepository by lazy { TaskRepository(database.taskDao()) }
    val pomoRepository: PomoRepository by lazy { PomoRepository(database.pomoSessionDao()) }
    val usageRepository: UsageRepository by lazy { UsageRepository(database.appUsageDao()) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            saveCrashLog(throwable)
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        }
        try {
            createNotificationChannels()
            scheduleAppUsageMonitoring()
        } catch (e: Exception) {
            saveCrashLog(e)
        }
    }

    private fun saveCrashLog(throwable: Throwable) {
        try {
            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val file = File(filesDir, "crash_log.txt")
            val writer = FileWriter(file, true)
            val printer = PrintWriter(writer)
            printer.println("===== CRASH at $timeStr =====")
            printer.println("Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DISPLAY})")
            printer.println("Android: ${Build.VERSION.SDK_INT}")
            printer.println("Error: ${throwable.javaClass.name}: ${throwable.message}")
            printer.println("Stack:")
            throwable.printStackTrace(printer)
            printer.println("================================")
            printer.println()
            printer.close()
        } catch (e: Exception) {
            Log.e("GuangYinRuSuo", "Failed to save crash log", e)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val timerChannel = NotificationChannel(CHANNEL_POMODORO_TIMER, "番茄钟倒计时", NotificationManager.IMPORTANCE_LOW).apply {
                description = "番茄钟前台服务的常驻倒计时通知"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            nm.createNotificationChannel(timerChannel)
            val completeChannel = NotificationChannel(CHANNEL_POMODORO_COMPLETE, "番茄钟完成", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "番茄钟专注/休息结束时的通知"
                enableVibration(true)
            }
            nm.createNotificationChannel(completeChannel)
            val monitorChannel = NotificationChannel(CHANNEL_APP_MONITOR, "应用使用提醒", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "娱乐APP超时使用的强制提醒"
            }
            nm.createNotificationChannel(monitorChannel)
        }
    }

    private fun scheduleAppUsageMonitoring() {
        val workRequest = PeriodicWorkRequestBuilder<AppUsageMonitorWorker>(15, TimeUnit.MINUTES)
            .addTag("app_usage_monitor").build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("app_usage_monitor", ExistingPeriodicWorkPolicy.KEEP, workRequest)
    }

    companion object {
        const val CHANNEL_POMODORO_TIMER = "pomodoro_timer"
        const val CHANNEL_POMODORO_COMPLETE = "pomodoro_complete"
        const val CHANNEL_APP_MONITOR = "app_monitor"
        @Volatile
        private var instance: GuangYinRuSuoApplication? = null
        fun getInstance(): GuangYinRuSuoApplication {
            return instance ?: throw IllegalStateException("GuangYinRuSuoApplication not initialized")
        }
    }
}
