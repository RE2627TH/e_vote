package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class CandidateApplicationRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("position") val position: String,
    @SerializedName("manifesto") val manifesto: String,
    @SerializedName("course") val course: String,
    @SerializedName("college") val college: String,
    @SerializedName("goals") val goals: String,
    @SerializedName("pledges") val pledges: String,
    @SerializedName("symbol_name") val symbolName: String,
    @SerializedName("photo") val photo: String?,
    @SerializedName("symbol") val symbol: String?
)