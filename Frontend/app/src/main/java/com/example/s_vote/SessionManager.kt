package com.example.s_vote

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "s_vote_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_STUDENT_ID = "student_id"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_SUBSCRIBED = "is_subscribed"
        private const val KEY_IS_PROFILE_COMPLETED = "is_profile_completed"
    }

    /**
     * Save user session
     */
    fun saveSession(token: String?, userId: String?, studentId: String?, role: String?, isSubscribed: Boolean = false, isProfileCompleted: Boolean = false) {
        val editor = prefs.edit()
        editor.putString(KEY_TOKEN, token)
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_STUDENT_ID, studentId)
        editor.putString(KEY_USER_ROLE, role)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putBoolean(KEY_IS_SUBSCRIBED, isSubscribed)
        editor.putBoolean(KEY_IS_PROFILE_COMPLETED, isProfileCompleted)
        editor.apply()
    }

    /**
     * Get Auth Token
     */
    fun getAuthToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    /**
     * Get User Role
     */
    fun getUserRole(): String? {
        return prefs.getString(KEY_USER_ROLE, null)
    }

    /**
     * Get User ID
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    /**
     * Get Student ID
     */
    fun getStudentId(): String? {
        return prefs.getString(KEY_STUDENT_ID, null)
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && !getAuthToken().isNullOrEmpty()
    }

    /**
     * Check if profile setup is completed
     */
    fun isProfileCompleted(): Boolean {
        return prefs.getBoolean(KEY_IS_PROFILE_COMPLETED, false) || getUserRole() == "admin"
    }

    /**
     * Mark profile as completed
     */
    fun setProfileCompleted() {
        prefs.edit().putBoolean(KEY_IS_PROFILE_COMPLETED, true).apply()
    }

    /**
     * Clear session on logout
     */
    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
