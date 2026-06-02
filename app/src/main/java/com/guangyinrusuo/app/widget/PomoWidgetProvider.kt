/*
 * 光阴如梭 - 个人效率工具
 * PomoWidgetProvider.kt
 *
 * 番茄钟快捷启动桌面小部件 Provider
 * 点击后直接开始一个25分钟番茄钟
 *
 * 交互：点击Widget → 发送广播 → WidgetClickReceiver → 打开番茄钟页面并自动开始
 * 或通过PendingIntent直接操作
 */

package com.guangyinrusuo.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.guangyinrusuo.app.R
import com.guangyinrusuo.app.receiver.WidgetClickReceiver

/**
 * 番茄钟快捷启动Widget
 *
 * 点击立即开始番茄钟专注（默认25分钟）
 * 实际启动由 WidgetClickReceiver 处理
 */
class PomoWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    /**
     * 更新Widget
     */
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_pomo)

        // 点击Widget启动番茄钟
        val clickIntent = Intent(context, WidgetClickReceiver::class.java).apply {
            action = WidgetClickReceiver.ACTION_OPEN_POMODORO
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, appWidgetId, clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(android.R.id.content, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
