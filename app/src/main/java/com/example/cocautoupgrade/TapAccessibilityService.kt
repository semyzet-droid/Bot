package com.example.cocautoupgrade

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

class TapAccessibilityService : AccessibilityService() {
    companion object { @Volatile var instance: TapAccessibilityService? = null }
    @Volatile private var foregroundPackage: String? = null
    override fun onServiceConnected() { super.onServiceConnected(); instance = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event?.eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            foregroundPackage = rootInActiveWindow?.packageName?.toString() ?: event.packageName?.toString()
        }
    }
    override fun onInterrupt() {}
    override fun onDestroy() { if (instance === this) instance = null; super.onDestroy() }
    fun gameIsForeground() = foregroundPackage == "com.supercell.clashofclans"
    fun tap(x: Float, y: Float, callback: (Boolean) -> Unit) {
        if (!gameIsForeground()) { callback(false); return }
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 90)).build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) { callback(true) }
            override fun onCancelled(gestureDescription: GestureDescription?) { callback(false) }
        }, null)
    }
}
