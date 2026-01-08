package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class LiveResult(
    @SerializedName("id") val id: Int,
    @SerializedName("candidate_name") val candidateName: String,
    @SerializedName("votes") val votes: Int,
    @SerializedName("position") val position: String,
    @SerializedName("department") val department: String
)