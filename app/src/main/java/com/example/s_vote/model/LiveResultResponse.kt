package com.example.s_vote.model

data class LiveResultResponse(
    val success: Boolean,
    val results: List<LiveResult>
)