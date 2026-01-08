package com.example.s_vote.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.s_vote.*
import com.example.s_vote.LoginScreen
import com.example.s_vote.RegistrationScreen
import com.example.s_vote.CandidateProfileViewScreen

@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {

        // ---------- SPLASH ----------
        composable(Routes.SPLASH) { SplashScreen(navController) }

        // ---------- AUTH ----------
        composable(Routes.LOGIN) { LoginScreen(navController) }
        composable(Routes.REGISTRATION) { RegistrationScreen(navController) }
        composable(Routes.FORGOT_PASSWORD) { ForgotPasswordScreen(navController) }

        // ---------- STUDENT ----------
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.MANIFESTO) { ManifestoScreen(navController) }
        composable(Routes.PROFILE) { ProfileScreen(navController) }
        composable(Routes.POLL_HISTORY) { PollHistoryScreen(navController) }
        composable(Routes.RESULT) { ResultScreen(navController) }
        composable(Routes.FAQ) { FAQScreen(navController) }

        composable(
            route = Routes.CANDIDATE_LIST,
            arguments = listOf(navArgument("roleName") { type = NavType.StringType })
        ) { backStackEntry ->
            val roleName = backStackEntry.arguments!!.getString("roleName")!!
            CandidateListingScreen(navController, roleName)
        }

        composable(
            route = Routes.CANDIDATE_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments!!.getString("id")!!
            CandidateProfileViewScreen(
                candidateId = id,
                navController = navController
            )
        }

        composable(
            route = Routes.SCAN_ID,
            arguments = listOf(
                navArgument("candidateId") { type = NavType.StringType },
                navArgument("position") { nullable = true }
            )
        ) { backStackEntry ->
            val candidateId = backStackEntry.arguments!!.getString("candidateId")!!
            val position = backStackEntry.arguments?.getString("position") ?: ""
            ScanIdScreen(
                navController = navController,
                candidateId = candidateId,
                position = position
            )
        }

        composable(Routes.VOTE_SUBMITTED) { VoteSubmittedScreen(navController) }

        composable(
            route = Routes.VOTE_CONFIRMATION,
            arguments = listOf(
                navArgument("candidateId") { type = NavType.StringType },
                navArgument("position") { nullable = true },
                navArgument("name") { nullable = true },
                navArgument("dept") { nullable = true },
                navArgument("id") { nullable = true }
            )
        ) { backStackEntry ->
            val candidateId = backStackEntry.arguments!!.getString("candidateId")!!
            val position = backStackEntry.arguments?.getString("position") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: "Unknown"
            val dept = backStackEntry.arguments?.getString("dept") ?: "Unknown"
            val id = backStackEntry.arguments?.getString("id") ?: "Unknown"
            VoteConfirmationScreen(
                navController = navController,
                candidateId = candidateId,
                position = position,
                studentName = name,
                department = dept,
                studentId = id
            )
        }

        composable(
            route = Routes.ID_VERIFIED,
            arguments = listOf(
                navArgument("candidateId") { type = NavType.StringType },
                navArgument("name") { nullable = true },
                navArgument("dept") { nullable = true },
                navArgument("id") { nullable = true },
                navArgument("position") { nullable = true }
            )
        ) { backStackEntry ->
            val candidateId = backStackEntry.arguments!!.getString("candidateId")!!
            IdVerifiedScreen(
                navController = navController,
                candidateId = candidateId,
                studentName = backStackEntry.arguments?.getString("name") ?: "Unknown",
                department = backStackEntry.arguments?.getString("dept") ?: "Unknown",
                studentId = backStackEntry.arguments?.getString("id") ?: "Unknown",
                position = backStackEntry.arguments?.getString("position") ?: "Unknown"
            )
        }

        // ---------- ADMIN ----------
        composable(Routes.ADMIN_HOME) { AdminDashboardScreen(navController) }
        composable(Routes.MANAGE_CANDIDATES) { AdminHomeScreen(navController) }
        composable(Routes.CREATE_ELECTION) { CreateElectionScreen(navController) }
        composable(Routes.ADMIN_RESULTS) { AdminResultsScreen(navController) }
        composable(Routes.REPORTS) { AdminReportsScreen(navController) }

        // ---------- CANDIDATE ----------
        composable(
            route = Routes.CANDIDATE_DASHBOARD,
            arguments = listOf(navArgument("candidateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val candidateId = backStackEntry.arguments!!.getString("candidateId")!!
            CandidateDashboardScreen(
                navController = navController,
                candidateId = candidateId
            )
        }

        // Candidate management / utility screens
        composable(
            route = Routes.CANDIDATE_PROFILE,
            arguments = listOf(navArgument("candidateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val candidateId = backStackEntry.arguments!!.getString("candidateId")!!
            ManageCandidateProfileScreen(navController = navController, candidateId = candidateId)
        }

        composable(
            route = Routes.CANDIDATE_FEEDBACK,
            arguments = listOf(navArgument("candidateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val candidateId = backStackEntry.arguments!!.getString("candidateId")!!
            ViewAllFeedbackScreen(navController = navController, candidateId = candidateId)
        }

        composable(
            route = Routes.CAMPAIGN_PREVIEW,
            arguments = listOf(navArgument("candidateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val candidateId = backStackEntry.arguments!!.getString("candidateId")!!
            CampaignPreviewScreen(navController = navController, candidateId = candidateId)
        }
        // In your navigation composable
        composable("candidate_feedback/{candidateId}") { backStackEntry ->
            val candidateId = backStackEntry.arguments?.getString("candidateId") ?: ""
            CandidateFeedbackScreen(navController, candidateId)
        }
        composable(
            route = Routes.CANDIDATE_APPLICATION,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments!!.getString("userId") ?: "0"
            CandidateApplicationScreen(navController = navController, userId = userId.toIntOrNull() ?: 0)
        }

        composable(
            route = Routes.APPLICATION_SUBMITTED,
            arguments = listOf(navArgument("appId") { type = NavType.StringType })
        ) { backStackEntry ->
            val appId = backStackEntry.arguments!!.getString("appId") ?: ""
            ApplicationSubmittedScreen(navController = navController, applicationId = appId)
        }

        composable(
            route = Routes.APPLICATION_STATUS,
            arguments = listOf(navArgument("appId") { type = NavType.StringType })
        ) { backStackEntry ->
             val appId = backStackEntry.arguments!!.getString("appId") ?: ""
            ApplicationStatusScreen(navController = navController, applicationId = appId)
        }
    }
}