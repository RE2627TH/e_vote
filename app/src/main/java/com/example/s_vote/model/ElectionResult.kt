package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class ElectionResult(
    @SerializedName("name") val name: String,
    @SerializedName("position") val position: String,
    @SerializedName("vote_count") val voteCount: String // or Int, depends on PHP output (count often string)
)
