package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class AppUser(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("department") val department: String? = null,
    @SerializedName("college") val college: String? = null,
    @SerializedName("dob") val dob: String? = null,
    @SerializedName("student_id") val studentId: String? = null,
    @SerializedName("is_subscribed") val isSubscribed: Int = 0,
    @SerializedName("payment_id") val paymentId: String? = null,
    @SerializedName("profile_photo") val profilePhoto: String? = null,
    @SerializedName("position") val position: String? = null,
    @SerializedName("application_status") val applicationStatus: String? = null,
    @SerializedName("candidate_details") val candidateDetails: Candidate? = null
)
