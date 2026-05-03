package com.example.leximaster.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

fun NavController.navigateToWordDiscovery(wordText: String = "") {
    val route = WordDiscoveryDest(wordText)
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = true
        }
    }
}

fun NavController.navigateToQuiz(sessionId: Long = 0L, sessionType: String = "NEW_TEST") {
    val route = QuizRoute(sessionId, sessionType)
    navigate(route) {
        popUpTo(graph.findStartDestination().id)
    }
}

fun NavController.navigateToAnalytics(wordId: Long? = null) {
    val route = AnalyticsRoute(wordId)
    navigate(route) {
        popUpTo<WordDiscoveryDest>()
    }
}

fun NavController.navigateToFeedback(sessionId: Long = 0L, wordId: Long? = null) {
    val route = FeedbackRoute(sessionId, wordId)
    navigate(route)
}

fun NavController.navigateBack() {
    popBackStack()
}
