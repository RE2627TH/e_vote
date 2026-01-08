package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class CandidateApplicationRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("dob") val dob: String,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("department") val department: String,
    @SerializedName("position") val position: String,
    @SerializedName("manifesto") val manifesto: String
)