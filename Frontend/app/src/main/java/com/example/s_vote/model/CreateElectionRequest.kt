package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class CreateElectionRequest(
    @SerializedName("title") val title: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String
)
