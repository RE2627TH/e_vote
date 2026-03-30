package com.example.s_vote.model

data class RegisterRequest(
    val name: String,
    val dob: String,
    val student_id: String, // Matches ID Number field
    val department: String,
    val email: String,
    val password: String,
    val role: String
)