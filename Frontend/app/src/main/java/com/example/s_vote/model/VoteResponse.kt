package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class VoteResponse(
    val success: Boolean,
    val message: String? = null,
    @SerializedName("vote_id")
    val voteId: String? = null,
    @SerializedName("candidate_name")
    val candidateName: String? = null,
    @SerializedName("timestamp")
    val timestamp: String? = null
)