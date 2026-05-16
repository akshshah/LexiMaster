package com.example.leximaster.presentation.ui.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leximaster.data.local.entity.UserProfileEntity
import com.example.leximaster.data.repository.LexiMasterRepository
import com.example.leximaster.data.repository.MasteryStage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserProfileState(
    val username: String = "",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalPoints: Int = 0,
    val noviceCount: Int = 0,
    val competentCount: Int = 0,
    val expertCount: Int = 0,
    val masteredCount: Int = 0,
    val isEditingUsername: Boolean = false,
    val usernameInput: String = "",
    val isLoading: Boolean = true,
) {
    val totalWords: Int get() = noviceCount + competentCount + expertCount + masteredCount
}

sealed interface ProfileAction {
    data object OnEditUsernameClick : ProfileAction
    data object OnDismissEditUsername : ProfileAction
    data class OnUsernameInputChange(val input: String) : ProfileAction
    data object OnSaveUsername : ProfileAction
}

class UserProfileViewModel(private val repository: LexiMasterRepository) : ViewModel() {

    // Holds transient UI state (dialog visibility, text input)
    private val _uiState = MutableStateFlow(UserProfileState())

    val state: StateFlow<UserProfileState> = combine(
        repository.observeUserProfile(),
        repository.getAllWords(),
        _uiState
    ) { profile, words, localUiState ->
        var novice = 0; var competent = 0; var expert = 0; var mastered = 0
        for (word in words) {
            when (MasteryStage.fromScore(word.masteryScore)) {
                MasteryStage.NOVICE -> novice++
                MasteryStage.COMPETENT -> competent++
                MasteryStage.EXPERT -> expert++
                MasteryStage.MASTERED -> mastered++
            }
        }
        localUiState.copy(
            username = profile?.username.orEmpty(),
            currentStreak = profile?.currentStreak ?: 0,
            longestStreak = profile?.longestStreak ?: 0,
            totalPoints = profile?.totalPoints ?: 0,
            noviceCount = novice,
            competentCount = competent,
            expertCount = expert,
            masteredCount = mastered,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserProfileState(),
    )

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnEditUsernameClick -> _uiState.update {
                it.copy(isEditingUsername = true, usernameInput = state.value.username)
            }
            ProfileAction.OnDismissEditUsername -> _uiState.update {
                it.copy(isEditingUsername = false, usernameInput = "")
            }
            is ProfileAction.OnUsernameInputChange -> _uiState.update {
                it.copy(usernameInput = action.input)
            }
            ProfileAction.OnSaveUsername -> {
                val newName = _uiState.value.usernameInput.trim()
                if (newName.isBlank()) return
                viewModelScope.launch {
                    repository.updateUsername(
                        id = UserProfileEntity.PROFILE_ID,
                        username = newName,
                    )
                    _uiState.update { it.copy(isEditingUsername = false, usernameInput = "") }
                }
            }
        }
    }
}
