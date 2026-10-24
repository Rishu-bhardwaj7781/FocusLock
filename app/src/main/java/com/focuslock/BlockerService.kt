package com.focuslock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class BlockerService : AccessibilityService() {

    private val launchers by lazy {
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        packageManager.queryIntentActivities(i, 0).map { it.activityInfo.packageName }.toSet()
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent?) {
        if (e?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = e.packageName?.toString() ?: return
        val st = Store(this)
        if (!st.active() || !shouldBlock(pkg, st)) return

        performGlobalAction(GLOBAL_ACTION_HOME)
        try {
            startActivity(Intent(this, BlockActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) { /* HOME action above still protects the session */ }
    }

    private fun shouldBlock(pkg: String, st: Store): Boolean {
        if (pkg in PROTECTED) return true
        if (pkg == packageName || pkg == "android" || pkg == "com.android.systemui") return false
        if (pkg in launchers) return false
        if (pkg.contains("inputmethod") || pkg.contains("keyboard")) return false
        if (pkg.contains("dialer") || pkg.contains("telecom") || pkg.contains("emergency")) return false
        val mode = MODES.first { it.key == st.modeKey }
        return when {
            mode.key == "custom" -> pkg in st.custom
            mode.blockAll -> pkg !in setOf("com.google.android.calculator", "com.google.android.keep", "com.android.calculator2")
            else -> pkg in mode.blocked
        }
    }

    override fun onInterrupt() {}
}
