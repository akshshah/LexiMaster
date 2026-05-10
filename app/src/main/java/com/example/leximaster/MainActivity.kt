package com.example.leximaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.leximaster.presentation.mainscreen.MainScreen
import com.example.leximaster.ui.theme.LexiMasterTheme
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LexiMasterTheme {
                // MainScreen now handles the NavController, Scaffold,
                // and BottomBar visibility logic.
                MainScreen()
            }
        }
    }
}