package com.example.leximaster.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.leximaster.presentation.library.LibraryAction
import com.example.leximaster.presentation.library.LibraryEvent
import com.example.leximaster.presentation.library.LibraryScreen
import com.example.leximaster.presentation.library.LibraryViewModel
import com.example.leximaster.presentation.word.WordDiscoveryScreen
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
            // Placeholder for Dashboard
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Dashboard Coming Soon")
            }
        }

        composable<LibraryRoute> {
            val viewModel = koinViewModel<LibraryViewModel>()
            val state by viewModel.state.collectAsState()

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
                onNavigateToDiscovery = { viewModel.onAction(LibraryAction.NavigateToWordDiscovery) }
            )
        }

        composable<ProfileRoute> {
            // Placeholder for Profile
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Profile Coming Soon")
            }
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