package com.guangyinrusuo.app.ui.pomodoro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.guangyinrusuo.app.R
import com.guangyinrusuo.app.data.db.entity.TaskEntity
import com.guangyinrusuo.app.databinding.FragmentPomodoroBinding

class PomodoroFragment : Fragment() {
    private var _b: FragmentPomodoroBinding? = null
    private val b get() = _b!!
    private lateinit var vm: PomodoroViewModel
    private var tl = mutableListOf<TaskEntity>()
    private lateinit var ta: android.widget.ArrayAdapter<String>

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View { _b = FragmentPomodoroBinding.inflate(i, c, false); return b.root }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        vm = ViewModelProvider(this)[PomodoroViewModel::class.java]
        b.btnStartPause.setOnClickListener { when (vm.state.value) { PomoState.IDLE -> vm.startFocus(gid()); PomoState.PAUSED -> vm.resumeFocus(); PomoState.FOCUSING -> vm.pauseFocus(); PomoState.BREAK -> vm.pauseFocus(); else -> {} } }
        b.btnStop.setOnClickListener { vm.stopFocus() }
        val n = mutableListOf("不限关联任务")
        ta = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, n).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        b.spinnerTask.adapter = ta
        vm.loadUncompletedTasks { tasks -> tl.clear(); tl.addAll(tasks); n.clear(); n.add("不限关联任务"); n.addAll(tasks.map { it.title }); ta.notifyDataSetChanged() }
        vm.state.observe(viewLifecycleOwner) { s ->
            when (s) {
                PomoState.IDLE -> { b.btnStartPause.text = "START"; b.btnStop.visibility = View.GONE; b.chipState.text = "准备就绪"; b.chipState.setBackgroundColor(resources.getColor(R.color.pomo_idle)); b.spinnerTask.isEnabled = true }
                PomoState.FOCUSING -> { b.btnStartPause.text = "PAUSE"; b.btnStop.visibility = View.VISIBLE; b.chipState.text = "专注中..."; b.chipState.setBackgroundColor(resources.getColor(R.color.pomo_focus)); b.spinnerTask.isEnabled = false }
                PomoState.PAUSED -> { b.btnStartPause.text = "RESUME"; b.chipState.text = "已暂停"; b.chipState.setBackgroundColor(resources.getColor(R.color.pomo_idle)) }
                PomoState.BREAK -> { b.btnStartPause.text = "PAUSE"; b.btnStop.visibility = View.VISIBLE; b.chipState.text = "休息中..."; b.chipState.setBackgroundColor(resources.getColor(R.color.pomo_break)); b.spinnerTask.isEnabled = false }
            }
        }
        vm.remainingSeconds.observe(viewLifecycleOwner) { s -> b.tvTimer.text = vm.getFormattedTime(s) }
        vm.todayFocusSeconds.observe(viewLifecycleOwner) { s -> b.tvTodayFocus.text = if (s/60>=60) "${s/3600}小时${s%3600/60}分钟" else "${s/60}分钟" }
        vm.todaySessionCount.observe(viewLifecycleOwner) { c -> b.tvTodaySessions.text = c.toString(); b.tvSessionCount.text = "今日完成: $c 个" }
    }
    private fun gid(): Long? { val p = b.spinnerTask.selectedItemPosition; return if (p > 0 && p <= tl.size) tl[p-1].id else null }
    override fun onResume() { super.onResume(); vm.loadTodayStats() }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
