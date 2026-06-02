package com.guangyinrusuo.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

val R = Color(0xFFC62828)
val G = Color(0xFF2E7D32)
val Gy = Color(0xFF757575)
val T1 = Color(0xFF1A1A1A)
val T3 = Color(0xFF999999)
val Bg = Color(0xFFF8F9FA)
val Bd = Color(0xFFEEEEEE)
val Cw = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var t by remember { mutableIntStateOf(0) }
    Scaffold(bottomBar = {
        NavigationBar(containerColor = Cw, tonalElevation = 0.dp) {
            val items = listOf("时间线" to Icons.Default.ViewTimeline, "番茄钟" to Icons.Default.Timer, "目标" to Icons.Default.TaskAlt, "统计" to Icons.Default.BarChart, "设置" to Icons.Default.Settings)
            items.forEachIndexed { i, (l, ic) ->
                NavigationBarItem(selected = t == i, onClick = { t = i }, icon = { Icon(ic, l) }, label = { Text(l, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = R, unselectedIconColor = T3, selectedTextColor = R, unselectedTextColor = T3, indicatorColor = Color.Transparent))
            }
        }
    }) { p ->
        Box(Modifier.fillMaxSize().padding(p).background(Bg)) {
            when (t) { 0 -> P0(); 1 -> P1(); 2 -> P2(); 3 -> P3(); 4 -> P4() }
        }
    }
}

@Composable
fun P0() {
    val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val mi = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE)
    Column(Modifier.fillMaxSize()) {
        Surface(color = Cw) { Text("时间线", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 10.dp)) }
        LazyColumn(Modifier.fillMaxSize()) {
            items(24) { hour ->
                val cur = hour == h
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).background(if (cur) Color(0xFFFFF8E1) else Color.Transparent).padding(start = 8.dp)) {
                    Column(Modifier.width(44.dp).padding(top = 6.dp), horizontalAlignment = Alignment.End) {
                        Text(if (cur) "此时" else String.format("%02d:00", hour), fontSize = 11.sp, color = if (cur) R else T3, fontWeight = if (cur) FontWeight.Bold else FontWeight.Normal)
                    }
                    Box(Modifier.width(1.dp).fillMaxHeight().background(Bd))
                    Box(Modifier.weight(1f).padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp)) {
                        if (cur) Box(Modifier.height(2.dp).fillMaxWidth(0.95f).offset(y = (mi * 56 / 60).dp).background(R))
                        if (hour == 8) { TC("背单词", "08:00", true) {}; TC("复习高数", "08:30", true) {} }
                        if (hour == 10) TC("微积分题", "10:00", false) {}
                        if (hour == 17) TC("跑步3公里", "17:00", false) {}
                    }
                }
                if (hour < 23) HorizontalDivider(thickness = 0.5.dp, color = Bd)
            }
        }
    }
}

@Composable fun TC(t: String, tm: String, d: Boolean, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(bottom = 3.dp).clickable(onClick = onClick), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Cw), elevation = CardDefaults.cardElevation(1.dp))
