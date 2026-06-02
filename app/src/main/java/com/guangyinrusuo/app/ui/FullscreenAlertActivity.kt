/*
 * 光阴如梭 - 个人效率工具
 * FullscreenAlertActivity.kt
 *
 * 娱乐APP超时全屏警告Activity
 * 设计目标：全屏覆盖、不可跳过、强制暂停
 *
 * 触发流程：
 * WorkManager检测到APP超时 → 发送通知 → 用户点击通知 → 启动此Activity
 * 或 WorkManager直接通过Intent启动此Activity
 *
 * 行为：
 * 1. 全屏显示，覆盖所有界面
 * 2. 显示超时APP名称和已用时间
 * 3. 90秒强制冷却倒计时（期间关闭按钮不可用）
 * 4. 倒计时结束后可关闭
 * 5. 关闭后回到桌面（noHistory=true，不会留在返回栈中）
 */

package com.guangyinrusuo.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.guangyinrusuo.app.R
import com.guangyinrusuo.app.databinding.ActivityFullscreenAlertBinding

/**
 * 全屏超时警告Activity
 *
 * 重要适配点：
 * - showWhenLocked + turnScreenOn: 即使在锁屏状态也能点亮屏幕显示
 * - 全屏主题: 隐藏状态栏和导航栏
 * - noHistory: 按返回键直接回到桌面，不留痕迹
 */
class FullscreenAlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullscreenAlertBinding

    companion object {
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_LIMIT_MINUTES = "extra_limit_minutes"
        // 强制关闭等待秒数
        const val FORCE_WAIT_SECONDS = 90
    }

    // 强制等待倒计时
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传入参数
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "未知应用"
        val limitMinutes = intent.getIntExtra(EXTRA_LIMIT_MINUTES, 30)

        // 显示超时信息
        binding.tvAlertMessage.text = getString(
            R.string.monitor_over_limit_message,
            appName,
            limitMinutes
        )

        // 开始强制等待倒计时
        startForceWaitCountdown()

        // 关闭按钮
        binding.btnDismiss.setOnClickListener {
            // 关闭超时APP（尝试通过系统Intent关闭，但Android限制较强）
            // 实际效果：回到桌面，让用户自行关闭
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    /**
     * 启动强制等待倒计时
     * 期间关闭按钮不可用
     */
    private fun startForceWaitCountdown() {
        binding.btnDismiss.isEnabled = false
        binding.tvCloseCountdown.text = "请等待 ${FORCE_WAIT_SECONDS} 秒后可关闭"

        countDownTimer = object : CountDownTimer(
            FORCE_WAIT_SECONDS * 1000L, 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val remaining = millisUntilFinished / 1000
                binding.tvCloseCountdown.text = getString(
                    R.string.monitor_over_limit_wait,
                    remaining.toInt()
                )
            }

            override fun onFinish() {
                binding.btnDismiss.isEnabled = true
                binding.tvCloseCountdown.text = "现在可以关闭了"
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
