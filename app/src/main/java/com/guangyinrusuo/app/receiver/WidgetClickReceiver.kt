/*
 * 光阴如梭 - 个人效率工具
 * WidgetClickReceiver.kt
 *
 * 桌面小部件点击事件广播接收器
 * 处理番茄钟Widget和时间线Widget的点击
 *
 * 点击行为：
 * - 时间线Widget点击 → 打开MainActivity的时间线Tab
 * - 番茄钟Widget点击 → 打开MainActivity的番茄钟Tab
 */

package com.guangyinrusuo.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.guangyinrusuo.app.ui.MainActivity

/**
 * Widget点击事件广播接收
 */
class WidgetClickReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_OPEN_TIMELINE = "com.guangyinrusuo.app.ACTION_OPEN_TIMELINE"
        const val ACTION_OPEN_POMODORO = "com.guangyinrusuo.app.ACTION_OPEN_POMODORO"

        // Intent Extra: 目标Tab位置
        const val EXTRA_NAV_TARGET = "extra_nav_target"
        const val NAV_TIMELINE = 0
        const val NAV_POMODORO = 1
    }

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_OPEN_TIMELINE -> {
                openAppWithTab(context, NAV_TIMELINE)
            }
            ACTION_OPEN_POMODORO -> {
                openAppWithTab(context, NAV_POMODORO)
            }
        }
    }

    /**
     * 打开应用并跳转到指定Tab
     */
    private fun openAppWithTab(context: Context, tabIndex: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAV_TARGET, tabIndex)
        }
        context.startActivity(intent)
    }
}
