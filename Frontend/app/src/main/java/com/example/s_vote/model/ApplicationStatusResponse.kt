package com.example.s_vote.model

data class ApplicationStatusResponse(val success: Boolean, val message: String, val application: Application? = null, val reason: String? = null)
data class Application(val status: String)