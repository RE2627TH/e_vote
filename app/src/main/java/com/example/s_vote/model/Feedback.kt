package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class Feedback(
    @SerializedName("id") val id: Int,
    @SerializedName("user_name") val userName: String,
    @SerializedName("rating") val rating: Double,
    @SerializedName("comment") val comment: String,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("time_ago") val timeAgo: String = "",
    val year: String = "",
    val major: String = "",
    val photoUrl: String = ""
)
