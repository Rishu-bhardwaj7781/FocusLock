package com.focuslock

import android.content.Context
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Local storage: accounts, the running session, custom app list and history. */
class Store(ctx: Context) {
    private val p = ctx.applicationContext.getSharedPreferences("focuslock", Context.MODE_PRIVATE)

    private fun hash(u: String, pw: String) =
        MessageDigest.getInstance("SHA-256").digest("fl:$u:$pw".toByteArray())
            .joinToString("") { "%02x".format(it) }

    fun signUp(u: String, pw: String): String? {
        if (u.isBlank() || pw.length < 6) return "Enter a username and a password of 6+ characters"
        if (p.contains("u_$u")) return "Username already taken"
        p.edit().putString("u_$u", hash(u, pw)).putString("current", u).apply()
        return null
    }

    fun login(u: String, pw: String): String? {
        if (p.getString("u_$u", null) != hash(u, pw)) return "Wrong username or password"
        p.edit().putString("current", u).apply()
        return null
    }

    var currentUser: String?
        get() = p.getString("current", null)
        set(v) { p.edit().putString("current", v).apply() }

    var endsAt: Long
        get() = p.getLong("endsAt", 0L)
        private set(v) { p.edit().putLong("endsAt", v).apply() }

    var modeKey: String
        get() = p.getString("mode", "study") ?: "study"
        set(v) { p.edit().putString("mode", v).apply() }

    var custom: Set<String>
        get() = p.getStringSet("custom", emptySet()) ?: emptySet()
        set(v) { p.edit().putStringSet("custom", v).apply() }

    val history: List<String>
        get() = (p.getString("history", "") ?: "").split("\n").filter { it.isNotBlank() }.takeLast(8).reversed()

    fun active() = System.currentTimeMillis() < endsAt

    /** Starts a locked session. There is intentionally no cancel function. */
    fun start(mode: String, minutes: Int) {
        endsAt = System.currentTimeMillis() + minutes * 60_000L
        modeKey = mode
        val line = "$mode · $minutes min · " + SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
        p.edit().putString("history", (p.getString("history", "") ?: "") + line + "\n").apply()
    }
}
