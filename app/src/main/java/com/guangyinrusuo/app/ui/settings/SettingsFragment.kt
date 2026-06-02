/*
 * 光阴如梭 - 个人效率工具
 * SettingsFragment.kt
 *
 * 设置页面Fragment
 * 包含番茄钟参数设置、娱乐APP管理、权限引导等
 */

package com.guangyinrusuo.app.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.guangyinrusuo.app.GuangYinRuSuoApplication
import com.guangyinrusuo.app.R
import com.guangyinrusuo.app.databinding.FragmentSettingsBinding

/**
 * 设置页面
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var app: GuangYinRuSuoApplication

    // 通知权限请求
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        updatePermissionUI()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireActivity().application as GuangYinRuSuoApplication

        setupSpinners()
        setupPermissionButtons()
        updatePermissionUI()

        // 管理被监控应用
        binding.btnManageMonitoredApps.setOnClickListener {
            // 打开系统设置中已安装应用列表（或后续版本跳转自定义管理页面）
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    /**
     * 设置番茄钟时长 Spinner
     */
    private fun setupSpinners() {
        val focusValues = intArrayOf(15, 20, 25, 30, 45, 60)
        val breakValues = intArrayOf(3, 5, 7, 10)
        val longBreakValues = intArrayOf(10, 15, 20, 30)

        val prefs = app.preferenceManager

        // 专注时长
        binding.spinnerFocusDuration.setSelection(
            focusValues.indexOf(prefs.focusDurationMinutes).coerceAtLeast(0)
        )
        binding.spinnerFocusDuration.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                prefs.focusDurationMinutes = focusValues[position]
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        // 短休息
        binding.spinnerShortBreak.setSelection(
            breakValues.indexOf(prefs.shortBreakMinutes).coerceAtLeast(0)
        )
        binding.spinnerShortBreak.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                prefs.shortBreakMinutes = breakValues[position]
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        // 长休息
        binding.spinnerLongBreak.setSelection(
            longBreakValues.indexOf(prefs.longBreakMinutes).coerceAtLeast(0)
        )
        binding.spinnerLongBreak.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                prefs.longBreakMinutes = longBreakValues[position]
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    /**
     * 设置权限按钮点击事件
     */
    private fun setupPermissionButtons() {
        // 通知权限
        binding.btnRequestNotification.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // UsageStats权限
        binding.btnRequestUsageStats.setOnClickListener {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }

        // 电池优化
        binding.btnRequestBattery.setOnClickListener {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = android.net.Uri.parse("package:${requireContext().packageName}")
            }
            startActivity(intent)
        }
    }

    /**
     * 更新权限状态显示
     */
    private fun updatePermissionUI() {
        // 通知权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotif = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            binding.btnRequestNotification.text = if (hasNotif) "✓ 已开启" else "去授权"
            binding.btnRequestNotification.isEnabled = !hasNotif
        } else {
            binding.btnRequestNotification.text = "✓ 已支持"
            binding.btnRequestNotification.isEnabled = false
        }

        // UsageStats权限
        val hasUsageStats = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val appOps = requireContext().getSystemService(android.app.AppOpsManager::class.java)
            val mode = appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                requireContext().packageName
            )
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } else true

        binding.btnRequestUsageStats.text = if (hasUsageStats) "✓ 已开启" else "去授权"
        binding.btnRequestUsageStats.isEnabled = !hasUsageStats
    }

    override fun onResume() {
        super.onResume()
        // 从设置页面返回时刷新权限状态
        updatePermissionUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
