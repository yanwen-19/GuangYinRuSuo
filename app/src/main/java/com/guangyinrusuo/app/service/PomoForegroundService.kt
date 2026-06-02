/*
 * 光阴如梭 - 个人效率工具
 * PomoForegroundService.kt
 *
 * 番茄钟前台服务 — 核心保活机制
 *
 * 为什么使用前台服务？
 * 澎湃OS（MIUI）对后台进程限制极其严格：
 * - 普通后台Service在几分钟内会被杀死
 * - 前台Service + 常驻通知栏 是唯一可靠的保活方式
 * - 通知栏显示倒计时剩余时间，用户可直观看到
 *
 * 生命周期：
 * startForegroundService() → onCreate() → onStartCommand() → 倒计时 → onDestroy()
 * 通过 ViewModel 中的协程控制倒计时，本服务仅负责保活和通知更新
 */

package com.guangyinrusuo.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.guangyinrusuo.app.GuangYinRuSuoApplication
import com.guangyinrusuo.app.R
import com.guangyinrusuo.app.receiver.PomoNotificationReceiver
import com.guangyinrusuo.app.ui.MainActivity

/**
 * 番茄钟前台服务
 *
 * Intent Extra 参数：
 * EXTRA_REMAINING_SECONDS: 倒计时剩余秒数
 * EXTRA_STATE: 当前状态 (FOCUSING / BREAK)
 */
class PomoForegroundService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val EXTRA_REMAINING_SECONDS = "remaining_seconds"
        const val EXTRA_STATE = "state"
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val remaining = intent?.getIntExtra(EXTRA_REMAINING_SECONDS, 0) ?: 0
        val state = intent?.getStringExtra(EXTRA_STATE) ?: "FOCUSING"

        // 构建并显示通知
        val notification = buildNotification(remaining, state)

        // 启动前台服务
        startForeground(NOTIFICATION_ID, notification)

        // 如果服务被系统杀死，不要自动重启（由ViewModel重新创建）
        return START_NOT_STICKY
    }

    /**
     * 构建常驻通知
     * 显示番茄钟倒计时剩余时间和控制按钮
     */
    private fun buildNotification(remainingSeconds: Int, state: String): Notification {
        val min = remainingSeconds / 60
        val sec = remainingSeconds % 60
        val timeStr = String.format("%02d:%02d", min, sec)

        val title = if (state == "FOCUSING") {
            "专注中 · $timeStr"
        } else {
            "休息中 · $timeStr"
        }

        // 点击通知打开主Activity
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 暂停按钮（通过BroadcastReceiver处理）
        val pauseIntent = Intent(this, PomoNotificationReceiver::class.java).apply {
            action = PomoNotificationReceiver.ACTION_PAUSE
        }
        val pausePendingIntent = PendingIntent.getBroadcast(
            this, 1, pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 停止按钮
        val stopIntent = Intent(this, PomoNotificationReceiver::class.java).apply {
            action = PomoNotificationReceiver.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, GuangYinRuSuoApplication.CHANNEL_POMODORO_TIMER)
            .setContentTitle(title)
            .setContentText("番茄钟正在运行")
            .setSmallIcon(R.drawable.ic_pomodoro)
            .setOngoing(true)  // 不可滑动清除
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_pause, "暂停", pausePendingIntent)
            .addAction(R.drawable.ic_stop, "停止", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)  // LOW = 不弹横幅
            .setSilent(true)  // 不发出声音
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
