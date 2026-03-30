package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class VerifyStudentRequest(
    @SerializedName("student_id")
    val studentId: String
)