package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

// UPDATED to match your ViewModel usage
data class VoteRequest(
    @SerializedName("voter_id")
    val userId: String,

    @SerializedName("candidate_id")
    val candidateId: String,

    @SerializedName("position")
    val position: String? = null,

    @SerializedName("election_id")
    val electionId: Int? = null
)