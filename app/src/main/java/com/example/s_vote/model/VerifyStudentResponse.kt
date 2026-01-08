package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class VerifyStudentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("verified") val verified: Boolean = false,
    @SerializedName("student_data") val studentData: StudentData? = null
)

data class StudentData(
    @SerializedName("name") val name: String,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("department") val department: String,
    @SerializedName("year") val year: String
)