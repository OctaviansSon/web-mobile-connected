package com.example.taskboard.data

import android.content.Context

const val DEFAULT_BASE_URL = "http://10.0.2.2:8000"   // 10.0.2.2 = the PC from inside the Android emulator

/** Token, server address and the signed-in user, kept in SharedPreferences. */
class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("planner_session", Context.MODE_PRIVATE)

    private fun putString(key: String, value: String?) {
        val editor = prefs.edit()
        if (value == null) editor.remove(key) else editor.putString(key, value)
        editor.apply()
    }

    var token: String?
        get() = prefs.getString("token", null)
        set(value) = putString("token", value)

    var baseUrl: String
        get() = prefs.getString("base_url", DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) = putString("base_url", value)

    var username: String?
        get() = prefs.getString("username", null)
        set(value) = putString("username", value)

    var email: String?
        get() = prefs.getString("email", null)
        set(value) = putString("email", value)

    var userId: Int
        get() = prefs.getInt("user_id", -1)
        set(value) { prefs.edit().putInt("user_id", value).apply() }

    /** Log out: forget the user but keep the server address. */
    fun clear() {
        val editor = prefs.edit()
        editor.remove("token").remove("username").remove("email").remove("user_id")
        editor.apply()
    }
}
