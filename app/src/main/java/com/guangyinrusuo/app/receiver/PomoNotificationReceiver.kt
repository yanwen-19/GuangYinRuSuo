/*
 * 光阴如梭 - 个人效率工具
 * PomoNotificationReceiver.kt
 *
 * 番茄钟通知栏按钮广播接收器
 * 处理用户点击通知栏上的「暂停」「停止」按钮
 *
 * 通过 BroadcastReceiver 接收 PendingIntent 发出的广播
 * 将操作转发给 MainActivity 或直接更新状态
 */

package com.guangyinrusuo.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.guangyinrusuo.app.service.PomoForegroundService

/**
 * 番茄钟通知控件广播接收
 */
class PomoNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PAUSE = "com.guangyinrusuo.app.ACTION_POMO_PAUSE"
        const val ACTION_STOP = "com.guangyinrusuo.app.ACTION_POMO_STOP"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_PAUSE -> {
                // 暂停番茄钟 — 停止前台服务
                // 实际暂停逻辑由 PomodoroViewModel 的 pauseFocus() 处理
                // 此处只需要停止前台服务
                context.stopService(Intent(context, PomoForegroundService::class.java))
            }
            ACTION_STOP -> {
                // 停止番茄钟 — 停止前台服务+清除状态
                context.stopService(Intent(context, PomoForegroundService::class.java))
                // 注：停止时的数据库记录由 ViewModel 在 onStop 中处理
            }
        }
    }
}
