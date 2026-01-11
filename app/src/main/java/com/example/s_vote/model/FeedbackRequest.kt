package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class FeedbackRequest(
    @SerializedName("candidate_id") val candidateId: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String
)
