package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class VoteHistoryItem(
    @SerializedName("position")
    val position: String,

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("candidate_name")
    val candidateName: String,

    @SerializedName("election_title")
    val electionTitle: String
)
