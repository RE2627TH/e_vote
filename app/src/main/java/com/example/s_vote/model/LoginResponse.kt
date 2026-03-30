package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val success: Boolean,
    val message: String?,   // ✅ nullable
    val user_id: Int? = null,
    val student_id: String? = null,
    val role: String? = null,
    val token: String? = null,
    val is_subscribed: Int = 0,
    val is_profile_completed: Int = 0,
    val redirect_to_form: Boolean? = null
)
