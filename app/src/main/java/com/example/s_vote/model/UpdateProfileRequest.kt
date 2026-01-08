package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("course") val course: String,
    @SerializedName("college") val college: String,
    @SerializedName("tagline") val tagline: String,
    @SerializedName("goals") val goals: String,
    @SerializedName("pledges") val pledges: String,
    @SerializedName("photo") val photo: String?,
    @SerializedName("symbol") val symbol: String?
)
