package com.example.s_vote.model

data class AdminApprovalRequest(val applicationId: Int, val approve: Boolean, val reason: String? = null)