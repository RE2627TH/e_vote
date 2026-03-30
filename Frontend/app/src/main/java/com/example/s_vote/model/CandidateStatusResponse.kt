package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class CandidateStatusResponse(
    @SerializedName("is_candidate") val isCandidate: Boolean,
    @SerializedName("status") val status: String? = null, // pending, approved, rejected
    @SerializedName("position") val position: String? = null,
    @SerializedName("application_id") val applicationId: Int? = null
)