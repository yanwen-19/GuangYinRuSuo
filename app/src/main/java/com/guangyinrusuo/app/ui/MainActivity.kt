/*
 * 光阴如梭 - 个人效率工具
 * MainActivity.kt
 *
 * 应用主Activity
 * 承载底部导航栏和4个Fragment页面
 *
 * 设计要点：
 * - 使用 Navigation Component 管理Fragment切换
 * - BottomNavigationView 绑定 NavController
 * - 处理挖孔屏适配 (fitsSystemWindows)
 * - 处理权限首次启动检查
 */

package com.guangyinrusuo.app.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.guangyinrusuo.app.R
import com.guangyinrusuo.app.databinding.ActivityMainBinding

/**
 * 应用主Activity
 *
 * 使用 ViewBinding 访问布局控件 (自动生成的类名 = activity_main → ActivityMainBinding)
 * 使用 Navigation Component 管理 Fragment 导航
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启用 ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置底部导航栏
        setupBottomNavigation()

        // 检测是否为首次启动
        checkFirstLaunch()
    }

    /**
     * 配置底部导航栏与 Navigation Component 的绑定
     *
     * 原理：
     * NavHostFragment 管理 Fragment 的导航栈
     * setupWithNavController 自动将 BottomNavigationView 的选中状态与导航目标同步
     */
    private fun setupBottomNavigation() {
        // 获取 NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController

        // 将底部导航与 NavController 绑定
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setupWithNavController(navController)

        // 监听导航变化，当页面切换时更新状态
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // 当前不在任何主页面时隐藏底部导航（如全屏弹窗）
            when (destination.id) {
                R.id.nav_timeline,
                R.id.nav_pomodoro,
                R.id.nav_stats,
                R.id.nav_settings -> bottomNav.visibility = android.view.View.VISIBLE
                else -> bottomNav.visibility = android.view.View.GONE
            }
        }
    }

    /**
     * 首次启动检查
     * - 检查通知权限 (Android 13+)
     * - 检查 UsageStats 权限
     * - 引导用户开启后台白名单
     *
     * 注意：这里只是触发检查，具体权限请求在 SettingsFragment 中处理
     */
    private fun checkFirstLaunch() {
        val prefs = getSharedPreferences("guangyinrusuo_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("first_launch", true)) {
            // 标记为非首次启动
            prefs.edit().putBoolean("first_launch", false).apply()

            // 如果有必要，可以在此展示引导页或权限说明
            // 目前权限引导分散在各自功能模块中按需触发
        }
    }

    /**
     * 检查 UsageStats 权限是否已开启
     * 这是娱乐监控模块的关键前置条件
     */
    fun hasUsageStatsPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val appOps = getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager
            val mode = appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
            return mode == android.app.AppOpsManager.MODE_ALLOWED
        }
        return true // Android 5.0 以下不需要此权限
    }

    /**
     * 打开 UsageStats 权限设置页
     * 引导用户手动开启「使用情况访问权限」
     */
    fun openUsageStatsSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
}
