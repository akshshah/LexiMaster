package com.example.leximaster.presentation.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.leximaster.presentation.ui.dashboard.DashboardEvent
import com.example.leximaster.presentation.ui.dashboard.DashboardScreen
import com.example.leximaster.presentation.ui.dashboard.DashboardViewModel
import com.example.leximaster.presentation.ui.library.LibraryEvent
import com.example.leximaster.presentation.ui.library.LibraryScreen
import com.example.leximaster.presentation.ui.library.LibraryViewModel
import com.example.leximaster.presentation.ui.quiz.QuizEffect
import com.example.leximaster.presentation.ui.quiz.QuizScreen
import com.example.leximaster.presentation.ui.quiz.QuizViewModel
import com.example.leximaster.presentation.ui.userprofile.UserProfileScreen
import com.example.leximaster.presentation.ui.userprofile.UserProfileViewModel
import com.example.leximaster.presentation.ui.wordDetail.WordDetailsEvent
import com.example.leximaster.presentation.ui.wordDetail.WordDetailsScreen
import com.example.leximaster.presentation.ui.wordDetail.WordDetailsViewModel
import com.example.leximaster.presentation.ui.wordDiscovery.WordDiscoveryScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun LexiNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
        modifier = modifier
    ) {
        // Dashboard Screen
        composable<DashboardRoute> {
            val viewModel = koinViewModel<DashboardViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            // Listen to the one-time events from the Channel
            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        is DashboardEvent.NavigateToQuiz -> {
                            navController.navigate(QuizRoute(sessionType = event.sessionType))
                        }
                    }
                }
            }

            DashboardScreen(
                state = state,
                onAction = viewModel::onAction,
            )
        }

        // Library Screen
        composable<LibraryRoute> {
            val viewModel = koinViewModel<LibraryViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            // Listen to the one-time events from the Channel
            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        is LibraryEvent.NavigateToWordDiscoveryEvent -> {
                            navController.navigate(WordDiscoveryRoute)
                        }
                        is LibraryEvent.NavigateToWordDetailEvent -> {
                            navController.navigate(WordDetailRoute(wordId = event.wordId))
                        }
                    }
                }
            }

            LibraryScreen(
                state = state,
                onAction = viewModel::onAction,
            )
        }

        // Profile Screen
        composable<ProfileRoute> {
            val viewModel = koinViewModel<UserProfileViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            UserProfileScreen(
                state = state,
                onAction = viewModel::onAction,
            )
        }

        // Word Discovery Screen
        composable<WordDiscoveryRoute> {
            WordDiscoveryScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Word Details Screen
        composable<WordDetailRoute> {
            val viewModel = koinViewModel<WordDetailsViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        WordDetailsEvent.NavigateBack -> { navController.popBackStack() }
                    }
                }
            }

            WordDetailsScreen(
                state = state,
                onAction = viewModel::onAction,
            )
        }

        // Quiz Screen
        composable<QuizRoute> {
            val viewModel = koinViewModel<QuizViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            // Handle side effects
            LaunchedEffect(Unit) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is QuizEffect.NavigateToDashboard -> {
                            navController.navigate(DashboardRoute) {
                                popUpTo(DashboardRoute) { inclusive = true }
                            }
                        }
                        is QuizEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }

            QuizScreen(
                state = state,
                onAction = viewModel::onAction,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}