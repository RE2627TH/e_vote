package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class AppUser(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("student_id") val studentId: String? = null,
    @SerializedName("candidate_details") val candidateDetails: Candidate? = null
)
