package com.example.s_vote.model


import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("user") val user: RegisteredUser? = null
)

data class RegisteredUser(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,
    val student_id: String? = null,
    val department: String? = null
)