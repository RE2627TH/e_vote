package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class GenericResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user_id") val user_id: String? = null
)