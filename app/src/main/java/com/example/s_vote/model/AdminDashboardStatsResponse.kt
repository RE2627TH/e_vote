package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class AdminDashboardStatsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("stats") val stats: AdminDashboardStats?
)

data class AdminDashboardStats(
    @SerializedName("students_count") val studentsCount: String,
    @SerializedName("candidates_count") val candidatesCount: String,
    @SerializedName("active_elections") val activeElections: String,
    @SerializedName("votes_cast") val votesCast: String,
    @SerializedName("pending_candidates") val pendingCandidates: String
)
