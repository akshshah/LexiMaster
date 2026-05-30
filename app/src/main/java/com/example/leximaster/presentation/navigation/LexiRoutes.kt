package com.example.leximaster.presentation.navigation

import com.example.leximaster.data.repository.SessionType
import kotlinx.serialization.Serializable

@Serializable
data object DashboardRoute

@Serializable
data object LibraryRoute

@Serializable
data object ProfileRoute

@Serializable
data object WordDiscoveryRoute

@Serializable
data class WordDetailRoute(val wordId: Long)

@Serializable
data class QuizRoute(val sessionType: SessionType)