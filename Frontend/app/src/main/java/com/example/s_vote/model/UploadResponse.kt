package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("file_path") val filePath: String?
)
