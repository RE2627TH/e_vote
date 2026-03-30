package com.example.s_vote.api

import com.example.s_vote.ElectionStatus
import com.example.s_vote.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    /* =======================
       1. AUTH
       ======================= */

    @POST("register.php")
    suspend fun registerUser(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("login.php")
    suspend fun loginUser(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("reset_password.php")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<GenericResponse>

    @POST("update_subscription.php")
    suspend fun updateSubscription(
        @Body request: SubscriptionRequest
    ): Response<GenericResponse>

    @POST("send_otp.php")
    suspend fun sendOtp(
        @Body request: SendOtpRequest
    ): Response<GenericResponse>

    @POST("verify_otp.php")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<GenericResponse>

    @POST("complete_profile_setup.php")
    suspend fun completeProfileSetup(
        @Body request: ProfileSetupRequest
    ): Response<GenericResponse>


    /* =======================
       2. CANDIDATE
       ======================= */

    @POST("submit_application.php")
    suspend fun submitApplication(
        @Body request: CandidateApplicationRequest
    ): Response<CandidateApplicationResponse>

    @POST("update_profile.php")
    suspend fun updateUserProfile(
        @Body request: UpdateUserProfileRequest
    ): Response<GenericResponse>

    @POST("update_candidate_profile.php")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<GenericResponse>

    @Multipart
    @POST("upload_image.php")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<UploadResponse>

    @POST("submit_feedback.php")
    suspend fun submitFeedback(
        @Body request: FeedbackRequest
    ): Response<GenericResponse>


    /* =======================
       3. VOTING
       ======================= */

    @POST("vote.php")
    suspend fun castVote(
        @Body request: VoteRequest
    ): Response<GenericResponse>


    /* =======================
       4. VERIFICATION
       ======================= */

    @POST("verify_student.php")
    suspend fun verifyStudent(
        @Body request: VerifyStudentRequest
    ): Response<VerifyStudentResponse>


    /* =======================
       5. GET DATA (USER)
       ======================= */

    @GET("get_candidates.php")
    suspend fun getCandidateList(): Response<List<Candidate>>

    // ✅ IMPORTANT: Profile uses ProfileResponse (NOT LoginResponse)
    @GET("get_profile.php")
    suspend fun getProfile(
        @Query("user_id") userId: String
    ): Response<ProfileResponse>

    @GET("get_vote_history.php")
    suspend fun getVoteHistory(
        @Query("voter_id") voterId: String
    ): Response<List<VoteHistoryItem>>


    /* =======================
       6. ADMIN
       ======================= */

    @GET("get_candidates.php")
    suspend fun getCandidatesByStatus(
        @Query("status") status: String,
        @Query("position") position: String = "ALL"
    ): Response<List<Candidate>>

    @GET("admin_approve.php")
    suspend fun manageCandidate(
        @Query("user_id") userId: String,
        @Query("action") action: String
    ): Response<GenericResponse>

    @GET("get_results.php")
    suspend fun getResults(
        @Query("user_id") userId: String,
        @Query("election_id") electionId: String? = null
    ): Response<List<ElectionResult>>

    @GET("get_elections.php")
    suspend fun getElections(): Response<List<ElectionStatus>>

    @POST("create_election.php")
    suspend fun createElection(
        @Body request: CreateElectionRequest
    ): Response<GenericResponse>

    @POST("update_election_status.php")
    suspend fun updateElectionStatus(
        @Body request: Map<String, String>
    ): Response<GenericResponse>

    @POST("delete_election.php")
    suspend fun deleteElection(
        @Body request: Map<String, String>
    ): Response<GenericResponse>

    @POST("publish_results.php")
    suspend fun publishResults(): Response<GenericResponse>

    @GET("get_active_election.php")
    suspend fun getActiveElection(): Response<ElectionStatus>

    @GET("dashboard_stats.php")
    suspend fun getDashboardStats(): Response<AdminDashboardStatsResponse>

    @GET("get_students.php")
    suspend fun getStudents(): Response<List<AppUser>>

    @GET("get_candidates.php")
    suspend fun getCandidateUsers(): Response<List<AppUser>>

    @POST("delete_student.php")
    suspend fun deleteUser(
        @Body request: Map<String, String>
    ): Response<GenericResponse>

    @POST("update_fcm_token.php")
    suspend fun updateFcmToken(
        @Body request: Map<String, String>
    ): Response<GenericResponse>

    @GET("get_notifications.php")
    suspend fun getNotifications(
        @Query("user_id") userId: String
    ): Response<com.example.s_vote.model.NotificationResponse>

    /* =======================
       7. OCR (PYTHON SERVER)
       ======================= */

    @Multipart
    @POST("ocr")
    suspend fun uploadOcrImage(
        @Part image: MultipartBody.Part
    ): Response<OcrResponse>
}
