package com.example.s_vote.api

data class DashboardResponse(
    val success: Boolean,
    val stats: Map<String, Any>? = null,
    val recent_candidates: List<Map<String, Any>>? = null,
    val recent_votes: List<Map<String, Any>>? = null,
    val timestamp: String? = null
)