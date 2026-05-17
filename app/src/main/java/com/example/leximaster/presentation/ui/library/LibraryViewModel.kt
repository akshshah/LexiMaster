package com.example.leximaster.presentation.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leximaster.data.local.entity.WordEntity
import com.example.leximaster.data.repository.LexiMasterRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryState(
    val isLoading: Boolean = true,
    val fullWordList: List<WordEntity> = emptyList(),
    val filteredWords: List<WordEntity> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

sealed interface LibraryAction {
    data class Search(val query: String) : LibraryAction
    data object NavigateToWordDiscovery : LibraryAction
    data class NavigateToWordDetail(val wordId: Long) : LibraryAction
}

sealed interface LibraryEvent {
    data object NavigateToWordDiscoveryEvent : LibraryEvent
    data class NavigateToWordDetailEvent(val wordId: Long) : LibraryEvent
}

class LibraryViewModel(
    private val repository: LexiMasterRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    // 2. Use a Channel for one-time UI events (prevents navigation loops)
    private val _eventChannel = Channel<LibraryEvent>()
    val events = _eventChannel.receiveAsFlow()

    init {
        loadAllWords()
    }

    fun onAction(action: LibraryAction) {
        when (action) {
            is LibraryAction.Search -> filterWords(action.query)
            is LibraryAction.NavigateToWordDiscovery -> {
                viewModelScope.launch {
                    _eventChannel.send(LibraryEvent.NavigateToWordDiscoveryEvent)
                }
            }
            is LibraryAction.NavigateToWordDetail -> {
                viewModelScope.launch {
                    _eventChannel.send(LibraryEvent.NavigateToWordDetailEvent(action.wordId))
                }
            }
        }
    }

    private fun loadAllWords() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.getAllWords().collectLatest { words ->
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            fullWordList = words,
                            // Re-apply filter in case the list updates while searching
                            filteredWords = filterList(words, currentState.searchQuery)
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Unknown Error") }
            }
        }
    }

    private fun filterWords(query: String) {
        _state.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredWords = filterList(currentState.fullWordList, query)
            )
        }
    }

    private fun filterList(words: List<WordEntity>, query: String): List<WordEntity> {
        return if (query.isBlank()) {
            words
        } else {
            words.filter { it.word.contains(query, ignoreCase = true) }
        }
    }
}