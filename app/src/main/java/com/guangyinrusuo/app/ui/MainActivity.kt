package com.guangyinrusuo.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guangyinrusuo.app.ui.theme.GuangYinRuSuoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GuangYinRuSuoTheme { MainScreen() } }
    }
}

val Red = Color(0xFFC62828)
val Green = Color(0xFF2E7D32)
val Gray = Color(0xFF757575)
val Tc = Color(0xFF1A1A1A)
val T2 = Color(0xFF666666)
val T3 = Color(0xFF999999)
val Bg = Color(0xFFF8F9FA)
val Bd = Color(0xFFEEEEEE)
val Cw = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var tab by remember { mutableIntStateOf(0) }
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Cw, tonalElevation = 0.dp) {
                listOf("时间线" to Icons.Default.ViewTimeline, "番茄钟" to Icons.Default.Timer, "目标" to Icons.Default.TaskAlt, "统计" to Icons.Default.BarChart, "设置" to Icons.Default.Settings).forEachIndexed { i, (l, ic) ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(ic, l) }, label = { Text(l, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Red, unselectedIconColor = T3, selectedTextColor = Red, unselectedTextColor = T3, indicatorColor = Color.Transparent))
                }
            }
        }) { p ->
        Box(Modifier.fillMaxSize().padding(p).background(Bg)) {
            when (tab) { 0 -> TimelinePage(); 1 -> PomoPage(); 2 -> GoalsPage(); 3 -> StatsPage(); 4 -> SettingsPage() }
        }
    }
}

// ===== 时间线 =====
@Composable
fun TimelinePage() {
    val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val m = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE)
    Column(Modifier.fillMaxSize()) {
        Surface(color = Cw) { Text("📋 时间线", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 10.dp)) }
        LazyColumn(Modifier.fillMaxSize()) {
            items(24) { hour ->
                val cur = hour == h
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).background(if (cur) Color(0xFFFFF8E1) else Color.Transparent).padding(start = 8.dp)) {
                    Column(Modifier.width(44.dp).padding(top = 6.dp), horizontalAlignment = Alignment.End) {
                        Text(if (cur) "此时" else String.format("%02d:00", hour), fontSize = 11.sp, color = if (cur) Red else T3, fontWeight = if (cur) FontWeight.Bold else FontWeight.Normal)
                    }
                    Box(Modifier.width(1.dp).fillMaxHeight().background(Bd))
                    Box(Modifier.weight(1f).padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp)) {
                        if (cur) Box(Modifier.height(2.dp).fillMaxWidth(0.95f).offset(y = (m * 56 / 60).dp).background(Red))
                        if (hour == 8) { TaskC("☕ 背单词", "08:00", true) {}; TaskC("📄 复习高数", "08:30", true) {} }
                        if (hour == 10) TaskC("📖 做微积分题", "10:00", false) {}
                        if (hour == 17) TaskC("🏃 跑步3公里", "17:00", false) {}
                    }
                }
                if (hour < 23) HorizontalDivider(thickness = 0.5.dp, color = Bd)
            }
        }
    }
}

@Composable fun TaskC(t: String, tm: String, d: Boolean, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(bottom = 3.dp).clickable(onClick = onClick), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Cw), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.padding(8.dp, 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (d) "✓" else "○", fontSize = 14.sp, color = if (d) Green else Red, modifier = Modifier.width(20.dp))
            Text(t, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (d) T3 else Tc, modifier = Modifier.weight(1f), textDecoration = if (d) TextDecoration.LineThrough else TextDecoration.None)
            Text(tm, fontSize = 10.sp, color = T3)
        }
    }
}

// ===== 番茄钟 =====
@Composable fun PomoPage() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(16.dp))
        Surface(shape = RoundedCornerShape(20.dp), color = Gray) { Text("准备就绪", modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp), color = Cw, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(24.dp))
        Text("25:00", fontSize = 52.sp, fontWeight = FontWeight.Bold, color = Red)
        Text("专注", fontSize = 11.sp, color = T3)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Surface(shape = CircleShape, color = Cw) { Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { Text("⏹", fontSize = 18.sp) } }
            Surface(shape = CircleShape, color = Red, shadowElevation = 4.dp) { Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) { Text("▶", fontSize = 26.sp, color = Cw) } }
        }
        Spacer(Modifier.height(20.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Cw)) {
            Text("关联待办", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp))
            Text("不限关联任务", fontSize = 13.sp, color = T3, modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 12.dp))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Cw)) {
                Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("0分钟", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Red); Text("今日专注", fontSize = 11.sp, color = T3) } }
            Card(Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Cw)) {
                Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("0", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF625B71)); Text("完成", fontSize = 11.sp, color = T3) } }
        }
    }
}

// ===== 目标 =====
@Composable fun GoalsPage() {
    Column(Modifier.fillMaxSize()) {
        Surface(color = Cw) { Text("🎯 目标树", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 10.dp)) }
        LazyColumn { items(listOf(
            "🌳 期末考进前10" to "日期·12/40" to 0.3f,
            "   📐 高数提分" to "日期·12/40" to 0.3f,
            "      📋 每天2道微积分题" to "日期·12/40" to 0.3f,
            "      📋 整理错题本" to "次数·5/20" to 0.25f,
            "   🏃 跑步10次" to "次数·3/10" to 0.3f,
            "   📖 每天背30个单词" to "日期·18/30" to 0.6f,
        )) { ((t, m), p) -> GoalC(t, m, p) } }
    }
}

@Composable fun GoalC(t: String, m: String, p: Float) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Cw)) {
        Row(Modifier.padding(12.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(t.trim(), fontSize = 13.sp, fontWeight = FontWeight.Medium); Text(m, fontSize = 10.sp, color = T3) }
            Column(horizontalAlignment = Alignment.End) {
                LinearProgressIndicator(p, Modifier.width(50.dp).height(4.dp), color = Green, trackColor = Bd, strokeCap = StrokeCap.Round)
                Text("${(p * 100).toInt()}%", fontSize = 10.sp, color = T2)
            }
        }
    }
}

// ===== 统计 =====
@Composable fun StatsPage() {
    Column(Modifier.fillMaxSize()) {
        Surface(color = Cw) { Text("📊 统计", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 10.dp)) }
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Cw)) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("0分钟", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Red); Text("今日专注", fontSize = 12.sp, color = T3) }
                }
            }
        }
    }
}

// ===== 设置 =====
@Composable fun SettingsPage() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Surface(color = Cw) { Text("⚙️ 设置", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 10.dp)) }
        Column(Modifier.padding(16.dp)) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Cw)) {
                Column { Text("🍅 番茄钟", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(16.dp, 14.dp, 16.dp, 4.dp))
                    listOf("专注时长" to "25 分钟", "短休息" to "5 分钟", "长休息" to "15 分钟").forEach { (l, v) -> SetRow(l, v) } }
            }
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Cw)) {
                Column { Text("🔐 权限", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(16.dp, 14.dp, 16.dp, 4.dp))
                    listOf("通知权限" to "✓ 已开启", "使用情况访问" to "去开启", "电池优化" to "去开启").forEach { (l, v) -> SetRow(l, v) } }
            }
        }
    }
}

@Composable fun SetRow(l: String, v: String) { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(l, fontSize = 13.sp, color = Tc); Text(v, fontSize = 13.sp, color = T3) }; HorizontalDivider(thickness = 0.5.dp, color = Bd, modifier = Modifier.padding(horizontal = 16.dp)) }
