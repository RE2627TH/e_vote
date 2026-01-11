package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class UpdateUserProfileRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("department") val department: String,
    @SerializedName("dob") val dob: String,
    @SerializedName("email") val email: String,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("college") val college: String
)
