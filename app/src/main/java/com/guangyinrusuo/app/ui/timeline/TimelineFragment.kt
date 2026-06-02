package com.guangyinrusuo.app.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.guangyinrusuo.app.adapter.HourlyTimelineAdapter
import com.guangyinrusuo.app.data.db.entity.TaskEntity
import com.guangyinrusuo.app.databinding.FragmentTimelineBinding
import java.util.Calendar

class TimelineFragment : Fragment() {
    private var _b: FragmentTimelineBinding? = null
    private val b get() = _b!!
    private lateinit var vm: TimelineViewModel
    private lateinit var ad: HourlyTimelineAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View { _b = FragmentTimelineBinding.inflate(i, c, false); return b.root }
    override fun onViewCreated(v: android.view.View, s: Bundle?) {
        super.onViewCreated(v, s)
        vm = ViewModelProvider(this)[TimelineViewModel::class.java]
        ad = HourlyTimelineAdapter(onTaskClick = { toggle(it) }, onTaskToggleDone = { toggle(it) })
        b.rvTimeline.apply { layoutManager = LinearLayoutManager(requireContext()); adapter = this@TimelineFragment.ad }
        b.btnAddTask.setOnClickListener { add() }
        b.etTaskInput.setOnEditorActionListener { _, a, _ -> if (a == EditorInfo.IME_ACTION_DONE) { add(); true } else false }
        vm.allTasks.observe(viewLifecycleOwner) { ad.setTasks(it) }
        vm.snackbarMessage.observe(viewLifecycleOwner) { msg -> msg?.let { Snackbar.make(b.root, it, Snackbar.LENGTH_SHORT).show(); vm.clearSnackbar() } }
    }
    private fun add() {
        val r = b.etTaskInput.text.toString().trim(); if (r.isEmpty()) return
        val m = Regex("""^(\d{1,2}):(\d{2})\s+(.+)$""").find(r)
        if (m != null) { val h=m.groupValues[1].toInt(); val mi=m.groupValues[2].toInt(); val t=m.groupValues[3].trim()
            if (h in 0..23 && mi in 0..59) { val c=Calendar.getInstance(); c.set(Calendar.HOUR_OF_DAY,h); c.set(Calendar.MINUTE,mi); c.set(Calendar.SECOND,0); c.set(Calendar.MILLISECOND,0); vm.addTask(t, dueDate=c.timeInMillis); b.etTaskInput.text?.clear(); return } }
        vm.addTask(r); b.etTaskInput.text?.clear()
    }
    private fun toggle(t: TaskEntity) { if (t.isCompleted) vm.unmarkAsCompleted(t) else vm.markAsCompleted(t) }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
