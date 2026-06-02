/*
 * 光阴如梭 - 个人效率工具
 * DisplayUtils.kt
 *
 * 显示适配工具类
 * 专为小米15挖孔屏、高刷新率、安全区适配
 *
 * 澎湃OS适配要点：
 * 1. 挖孔屏：使用 WindowInsets 获取安全区域，UI布局避开摄像头区域
 * 2. 120Hz：检测并请求最高刷新率，确保倒计时动画流畅
 * 3. 暗黑模式：通过资源文件夹自动适配 (values-night)
 * 4. 导航栏：透明导航栏 + 手势提示适配
 */

package com.guangyinrusuo.app.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.view.Display
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics

/**
 * 显示适配工具类
 */
object DisplayUtils {

    /**
     * 检查当前是否为暗黑模式
     * Material 3 主题会自动跟随系统切换，但某些自定义View需要手动判断
     */
    fun isDarkMode(context: Context): Boolean {
        return context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * 获取屏幕刷新率 (Hz)
     * 小米15屏幕为120Hz LTPO，部分场景可能降至60Hz
     * 番茄钟倒计时动画建议强制开启120Hz
     */
    fun getScreenRefreshRate(context: Context): Float {
        val display = getDisplay(context) ?: return 60f
        return display.refreshRate
    }

    /**
     * 检查是否支持高刷新率 (>= 90Hz)
     */
    fun isHighRefreshRate(context: Context): Boolean {
        return getScreenRefreshRate(context) >= 90f
    }

    /**
     * 获取屏幕显示区域（排除挖孔安全区）
     * 用于手动适配挖孔屏的安全显示区域
     *
     * @return Pair(usableWidth, usableHeight) — 可安全显示内容的区域
     */
    fun getUsableScreenSize(context: Context): Pair<Int, Int> {
        val display = getDisplay(context) ?: return Pair(0, 0)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 使用 WindowMetrics 获取
            val windowMetrics: WindowMetrics = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                windowManager.currentWindowMetrics
            } else {
                return Pair(display.width, display.height)
            }
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            Pair(
                windowMetrics.bounds.width() - insets.left - insets.right,
                windowMetrics.bounds.height() - insets.top - insets.bottom
            )
        } else {
            // Android 10 及以下使用 Display.getSize
            val size = Point()
            display.getSize(size)
            Pair(size.x, size.y)
        }
    }

    /**
     * 获取 Display 对象
     */
    private fun getDisplay(context: Context): Display? {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return windowManager.defaultDisplay
    }
}
