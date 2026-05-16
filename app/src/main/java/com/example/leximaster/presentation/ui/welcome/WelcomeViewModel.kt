package com.example.leximaster.presentation.ui.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leximaster.data.repository.LexiMasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WelcomeState(
    val username: String = "",
    val isLoading: Boolean = false,
    val isConfirmEnabled: Boolean = false,
)

sealed interface WelcomeAction {
    data class OnUsernameChanged(val username: String) : WelcomeAction
    data object OnConfirmClicked : WelcomeAction
}

class WelcomeViewModel(
    private val repository: LexiMasterRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WelcomeState())
    val state = _state.asStateFlow()

    fun onAction(action: WelcomeAction) {
        when (action) {
            is WelcomeAction.OnUsernameChanged -> {
                _state.update {
                    it.copy(
                        username = action.username,
                        isConfirmEnabled = action.username.isNotBlank(),
                    )
                }
            }

            is WelcomeAction.OnConfirmClicked -> saveProfile()
        }
    }

    private fun saveProfile() {
        val username = _state.value.username.trim()
        if (username.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.createUserProfile(username)
            // No need to navigate — MainScreen observes the profile
            // and will automatically hide WelcomeScreen once profile exists
            _state.update { it.copy(isLoading = false) }
        }
    }
}
