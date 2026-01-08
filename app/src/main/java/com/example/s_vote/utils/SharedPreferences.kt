package com.example.s_vote.utils

import android.content.Context

class SharedPrefManager(context: Context) {
    private val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getToken(): String? = prefs.getString("auth_token", null)
}