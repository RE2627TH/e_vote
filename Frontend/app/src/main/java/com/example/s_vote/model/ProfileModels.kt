package com.example.s_vote.model

data class ProfileSetupRequest(
    val user_id: String,
    val college: String,
    val profile_photo: String? // Base64 string
)
