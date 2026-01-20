package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val success: Boolean,
    val message: String?,   // ✅ nullable
    val user_id: Int? = null,
    val student_id: String? = null,
    val role: String? = null
)
