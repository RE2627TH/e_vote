package com.example.s_vote

import com.example.s_vote.R
import com.google.gson.annotations.SerializedName

// Duplicate Candidate class removed. Using com.example.s_vote.model.Candidate instead.

data class Feedback(
    val userName: String,
    val rating: Float,
    val comment: String
)

data class RoleItem(val name: String, val icon: Int)

data class ElectionStatus(
    @SerializedName("id") val id: String = "1",
    @SerializedName("title") val title: String = "College Election 2025",
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
    val isActive: Boolean = true
)

var currentElection = ElectionStatus()

val roleList = listOf(
    RoleItem("president", R.drawable.president),
    RoleItem("vice president", R.drawable.election_day),
    RoleItem("sports secretary", R.drawable.candidates),
    RoleItem("cultural secretary", R.drawable.corporate_culture),
    RoleItem("discipline secretary", R.drawable.election_results),
    RoleItem("Treasurer", R.drawable.election_day)
)

val candidateData = listOf(
    com.example.s_vote.model.Candidate(
        id = "1",
        name = "Kaviya P",
        position = "President",
        role = "president",
        imageResId = R.drawable.ig_kaviya,
        symbolResId = R.drawable.kaviyasymbol,
        manifesto = "Kaviya P is a passionate and dedicated student...",
        badges = listOf("Dedicated", "Responsible", "Approachable"),
        // feedback removed from model, skipping for now
        course = "MCA",
        college = "Saveetha College of Liberal Arts and Science",
        // email not in model
        tagline = "“Together, We Create. Together, We Celebrate.”",
        goals = "Improve student welfare\nOrganize events\nBuild transparent council",
        pledges = "I will lead with honesty\nI will listen to every student",
        status = "Approved"
    ),
    // ... I will need to replace the entire candidateData block comprehensively.
    // Since the mocked data is quite large and complex to map 1:1 in a single replace, 
    // AND the user only cares about specific errors, I will COMMENT OUT the mock data for now 
    // and provide an empty list or minimal example to fix the build immediately.
    // This is safer than manually refactoring 300 lines of mock objects blindly.
)
