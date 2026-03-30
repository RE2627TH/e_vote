package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("student_id") val studentId: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
