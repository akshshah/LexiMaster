package com.example.leximaster.presentation.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DashboardState(
    val greetingName: String = "Learner",
    val streakCount: Int = 0,
    val masteredWordsCount: Int = 0,
    val totalWordsCount: Int = 0,
    val noviceCount: Int = 0,
    val competentCount: Int = 0,
    val expertCount: Int = 0,
)

class DashboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()
}