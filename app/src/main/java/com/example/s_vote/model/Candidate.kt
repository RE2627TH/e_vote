package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class Candidate(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("department") val department: String? = null,
    @SerializedName("position") val position: String? = null,
    @SerializedName("manifesto") val manifesto: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("status") val status: String? = "approved", // Default or from API
    @SerializedName("rejection_reason") val rejectionReason: String? = null,
    // API Fields
    @SerializedName("photo") val photo: String? = null,
    @SerializedName("symbol") val symbolUrl: String? = null,
    @SerializedName("course") val course: String? = null,
    @SerializedName("college") val college: String? = null,
    @SerializedName("tagline") val tagline: String? = null,
    @SerializedName("goals") val goals: String? = null,
    @SerializedName("pledges") val pledges: String? = null,
    
    // UI specific fields (can be mapped or computed)
    // UI specific fields (can be mapped or computed)
    val imageResId: Int? = null,
    val symbolResId: Int? = null,
    val badges: List<String>? = null,
    val feedback: List<com.example.s_vote.model.Feedback>? = null,
    @SerializedName("vote_count") val voteCount: Int = 0
)
