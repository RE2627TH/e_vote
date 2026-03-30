package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class CandidateApplicationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("application_id") val applicationId: String? = null,
    @SerializedName("status") val status: String? = null // pending, approved, rejected
)