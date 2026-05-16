package com.example.leximaster.presentation.ui.mainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leximaster.data.local.entity.UserProfileEntity
import com.example.leximaster.data.repository.LexiMasterRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainScreenViewModel(
    repository: LexiMasterRepository,
) : ViewModel() {

    val userProfile: StateFlow<UserProfileEntity?> = repository
        .observeUserProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    // Becomes true after the first emission (null or not),
    // preventing a flicker to WelcomeScreen on cold start
    val isProfileLoaded: StateFlow<Boolean> = repository
        .observeUserProfile()
        .map { true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )
}
