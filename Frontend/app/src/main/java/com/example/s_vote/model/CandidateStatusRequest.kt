package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class CandidateStatusRequest(
    @SerializedName("user_id") val userId: Int
)