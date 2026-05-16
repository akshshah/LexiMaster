package com.example.leximaster.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.leximaster.presentation.ui.dashboard.DashboardScreen
import com.example.leximaster.presentation.ui.dashboard.DashboardViewModel
import com.example.leximaster.presentation.ui.library.LibraryEvent
import com.example.leximaster.presentation.ui.library.LibraryScreen
import com.example.leximaster.presentation.ui.library.LibraryViewModel
import com.example.leximaster.presentation.ui.userprofile.UserProfileScreen
import com.example.leximaster.presentation.ui.userprofile.UserProfileViewModel
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
        composable<DashboardRoute> {
            val viewModel = koinViewModel<DashboardViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            DashboardScreen(
                state = state,
            )
        }

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
                    }
                }
            }

            LibraryScreen(
                state = state,
                onAction = viewModel::onAction,
            )
        }

        composable<ProfileRoute> {
            val viewModel = koinViewModel<UserProfileViewModel>()

            UserProfileScreen(
                onAction = viewModel::onAction,
            )
        }

        composable<WordDiscoveryRoute> {
            WordDiscoveryScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}