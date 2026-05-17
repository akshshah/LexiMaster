package com.example.leximaster.presentation.ui.mainscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.leximaster.presentation.navigation.BottomNavigation
import com.example.leximaster.presentation.navigation.LexiNavHost
import com.example.leximaster.presentation.navigation.WordDetailRoute
import com.example.leximaster.presentation.navigation.WordDiscoveryRoute
import com.example.leximaster.presentation.ui.welcome.WelcomeScreen
import com.example.leximaster.presentation.ui.welcome.WelcomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen() {
    val welcomeViewModel = koinViewModel<WelcomeViewModel>()
    val welcomeState by welcomeViewModel.state.collectAsStateWithLifecycle()

    val mainScreenViewModel = koinViewModel<MainScreenViewModel>()
    val userProfile by mainScreenViewModel.userProfile.collectAsStateWithLifecycle()
    val isProfileLoaded by mainScreenViewModel.isProfileLoaded.collectAsStateWithLifecycle()

    // Show nothing until we know whether a profile exists (avoids flicker)
    if (!isProfileLoaded) return

    if (userProfile == null) {
        WelcomeScreen(
            state = welcomeState,
            onAction = welcomeViewModel::onAction,
        )
        return
    }

    // Profile exists — show main app
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.hasRoute<WordDiscoveryRoute>() != true
            && currentDestination?.hasRoute<WordDetailRoute>() != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigation(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp,
            )
        ) {
            LexiNavHost(
                navController = navController,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
