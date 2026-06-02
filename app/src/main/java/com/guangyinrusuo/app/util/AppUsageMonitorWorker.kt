package com.guangyinrusuo.app.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.guangyinrusuo.app.GuangYinRuSuoApplication
import com.guangyinrusuo.app.R
import com.guangyinrusuo.app.data.db.entity.AppUsageEntity
import com.guangyinrusuo.app.data.preferences.PreferenceManager
import com.guangyinrusuo.app.ui.FullscreenAlertActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppUsageMonitorWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val app = applicationContext as GuangYinRuSuoApplication
    private val prefs = PreferenceManager(applicationContext)
    private val repo = app.usageRepository

    override suspend fun doWork(): Result {
        return try {
            if (!hasPermission()) return Result.success()
            val usm = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val end = System.currentTimeMillis()
            val start = end - 24 * 60 * 60 * 1000
            val list = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end) ?: return Result.success()
            val today = df.format(Date())
            val monitored = prefs.getMonitoredPackages()
            for (stats in list) {
                val pkg = stats.packageName ?: continue
                val name = getAppName(pkg)
                val ms = stats.totalTimeInForeground
                if (ms <= 0) continue
                repo.saveRecord(AppUsageEntity(packageName = pkg, appName = name, totalTimeInForeground = ms, date = today))
                if (pkg in monitored) {
                    val limit = prefs.getMonitoredAppLimit(pkg) * 60 * 1000L
                    if (ms > limit) sendAlert(name, pkg, prefs.getMonitoredAppLimit(pkg))
                }
            }
            Result.success()
        } catch (e: SecurityException) { Result.success()
        } catch (e: Exception) { Result.retry() }
    }

    private fun getAppName(pkg: String): String = try { applicationContext.packageManager.getApplicationLabel(applicationContext.packageManager.getApplicationInfo(pkg, 0)).toString() } catch (e: Exception) { pkg }

    private fun sendAlert(name: String, pkg: String, limit: Int) {
        val intent = Intent(applicationContext, FullscreenAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("extra_app_name", name)
            putExtra("extra_package_name", pkg)
            putExtra("extra_limit_minutes", limit)
        }
        val pi = PendingIntent.getActivity(applicationContext, pkg.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(applicationContext, GuangYinRuSuoApplication.CHANNEL_APP_MONITOR)
            .setSmallIcon(R.drawable.ic_stop).setContentTitle("${name} 使用超时").setContentText("已超过每日限额 ${limit} 分钟").setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).setContentIntent(pi).build()
        (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(pkg.hashCode(), n)
    }

    private fun hasPermission(): Boolean {
        val ao = applicationContext.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        return ao.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), applicationContext.packageName) == android.app.AppOpsManager.MODE_ALLOWED
    }
}
