package com.example.s_vote.model

data class Feedback(
    val id: Int,
    val userName: String,
    val rating: Double,
    val comment: String,
    val timeAgo: String = "",
    val year: String = "",
    val major: String = "",
    val photoUrl: String = ""
)
