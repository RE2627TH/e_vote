package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: T? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("error_code") val errorCode: String? = null
)