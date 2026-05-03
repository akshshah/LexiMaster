package com.example.leximaster.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object WordDiscoveryRoute

@Serializable
data class WordDiscoveryDest(
    val wordText: String = "",
)

@Serializable
data class QuizRoute(
    val sessionId: Long = 0L,
    val sessionType: String = "NEW_TEST",
)

@Serializable
data class AnalyticsRoute(
    val wordId: Long? = null,
)

@Serializable
data class FeedbackRoute(
    val sessionId: Long = 0L,
    val wordId: Long? = null,
)
