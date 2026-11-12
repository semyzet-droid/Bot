package com.example.cocautoupgrade

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.InputType
import android.widget.*

class MainActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    private var index = 0
    private var cyclesDone = 0
    private var points: List<Pair<Float, Float>> = emptyList()
    private var pauseMs = 2000L
    private var cycles = 1
    private lateinit var status: TextView
    private lateinit var coordinates: EditText
    private lateinit var delay: EditText
    private lateinit var count: EditText
    private val prefs by lazy { getSharedPreferences("config", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24,24,24,24) }
        setContentView(ScrollView(this).apply { addView(root) })
        fun label(value: String): TextView = TextView(this).apply { text = value; textSize = 17f; setPadding(0,12,0,12); root.addView(this) }
        label("CoC TH10 · Prototype")
        label("Manual tap sequences only. Autonomous attacks and upgrades are NOT implemented.")
        status = label("Stopped")
        root.addView(Button(this).apply { text = "Enable accessibility service"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } })
        label("Coordinates (x,y), one pair per line")
        coordinates = EditText(this).apply {
            setText(prefs.getString("coords", ""))
            hint = "500,900"
            minLines = 4
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        root.addView(coordinates)
        label("Delay between taps (ms)")
        delay = EditText(this).apply { setText(prefs.getString("delay","2000")); inputType = InputType.TYPE_CLASS_NUMBER }
        root.addView(delay)
        label("Cycles (1–20)")
        count = EditText(this).apply { setText(prefs.getString("cycles","1")); inputType = InputType.TYPE_CLASS_NUMBER }
        root.addView(count)
        root.addView(Button(this).apply { text = "Save"; setOnClickListener { save(); status.text = "Saved" } })
        root.addView(Button(this).apply { text = "Start (open Clash of Clans within 5 seconds)"; setOnClickListener { start() } })
        root.addView(Button(this).apply { text = "Stop"; setOnClickListener { stop("Stopped by user") } })
        label("TH10 upgrade checklist")
        UpgradePlanner.priorities.forEachIndexed { i, title ->
            root.addView(CheckBox(this).apply {
                text = title
                isChecked = prefs.getBoolean("goal_$i",false)
                setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("goal_$i",checked).apply() }
            })
        }
        label("Warning: game automation may violate Supercell rules. Enable Accessibility only if you trust the code.")
    }
    private fun save() {
        prefs.edit().putString("coords",coordinates.text.toString()).putString("delay",delay.text.toString())
            .putString("cycles",count.text.toString()).apply()
    }
    private fun start() {
        if (running) return
        if (TapAccessibilityService.instance == null) { status.text = "Enable accessibility first"; return }
        try {
            points = coordinates.text.toString().lines().filter { it.isNotBlank() }.map {
                val values = it.split(",")
                require(values.size == 2)
                val x = values[0].trim().toFloat()
                val y = values[1].trim().toFloat()
                require(x.isFinite() && y.isFinite() && x >= 0f && y >= 0f)
                x to y
            }
            require(points.isNotEmpty() && points.size <= 50)
            pauseMs = (delay.text.toString().toLongOrNull() ?: 2000L).coerceIn(500L,60000L)
            cycles = (count.text.toString().toIntOrNull() ?: 1).coerceIn(1,20)
        } catch (_: Exception) { status.text = "Invalid coordinates"; return }
        save()
        running = true; index = 0; cyclesDone = 0
        status.text = "Starting in 5 seconds"
        handler.postDelayed({ next() },5000)
    }
    private fun next() {
        if (!running) return
        val service = TapAccessibilityService.instance
        if (service == null || !service.gameIsForeground()) { stop("Stopped: game not in foreground"); return }
        if (index >= points.size) {
            cyclesDone++
            if (cyclesDone >= cycles) { stop("Completed"); return }
            index = 0
        }
        val point = points[index++]
        service.tap(point.first,point.second) { success ->
            if (!success) stop("Tap failed")
            else if (running) handler.postDelayed({ next() },pauseMs)
        }
    }
    private fun stop(message: String) { running = false; handler.removeCallbacksAndMessages(null); status.text = message }
    override fun onDestroy() { stop("Stopped"); super.onDestroy() }
}
