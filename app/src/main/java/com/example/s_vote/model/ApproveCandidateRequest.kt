package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class ApproveCandidateRequest(
    @SerializedName("application_id") val applicationId: Int,
    @SerializedName("admin_id") val adminId: Int
)