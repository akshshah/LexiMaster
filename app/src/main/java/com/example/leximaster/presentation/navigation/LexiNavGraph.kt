package com.example.leximaster.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.example.leximaster.presentation.word.WordDiscoveryScreen
import kotlin.reflect.typeOf

@Composable
fun LexiNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: WordDiscoveryDest = WordDiscoveryDest(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        addWordDiscoveryScreen()
        addQuizScreen()
        addAnalyticsScreen()
        addFeedbackScreen()
    }
}

fun NavGraphBuilder.addWordDiscoveryScreen() {
    composable<WordDiscoveryDest>(
        deepLinks = listOf(
            navDeepLink<WordDiscoveryDest>(basePath = "leximaster://word_discovery"),
        ),
    ) {
        WordDiscoveryScreen()
    }
}

fun NavGraphBuilder.addQuizScreen() {
    composable<QuizRoute>(
        typeMap = mapOf(typeOf<Long>() to NavType.LongType),
        deepLinks = listOf(navDeepLink<QuizRoute>(basePath = "leximaster://quiz")),
    ) {
        // Quiz Screen will be implemented later
        Unit
    }
}

fun NavGraphBuilder.addAnalyticsScreen() {
    composable<AnalyticsRoute>(
        typeMap = mapOf(typeOf<Long>() to NavType.LongType),
        deepLinks = listOf(navDeepLink<AnalyticsRoute>(basePath = "leximaster://analytics")),
    ) {
        // Analytics Screen will be implemented later
        Unit
    }
}

fun NavGraphBuilder.addFeedbackScreen() {
    composable<FeedbackRoute>(
        typeMap = mapOf(typeOf<Long>() to NavType.LongType),
        deepLinks = listOf(navDeepLink<FeedbackRoute>(basePath = "leximaster://feedback")),
    ) {
        // Feedback Screen will be implemented later
        Unit
    }
}
