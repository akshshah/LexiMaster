package com.example.leximaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.leximaster.presentation.navigation.LexiNavHost
import com.example.leximaster.presentation.navigation.WordDiscoveryDest
import com.example.leximaster.ui.theme.LexiMasterTheme
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LexiMasterTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LexiNavHost(
                        modifier = Modifier.fillMaxSize(),
                        navController = navController,
                        startDestination = WordDiscoveryDest(),
                    )
                }
            }
        }
    }
}