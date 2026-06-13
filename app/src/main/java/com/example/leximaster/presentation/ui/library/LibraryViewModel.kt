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
    val sortType: SortType = SortType.ALPHABETICAL_ASC,
    val error: String? = null
)


sealed interface LibraryAction {
    data class Search(val query: String) : LibraryAction
    data class Sort(val sortType: SortType) : LibraryAction
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
            is LibraryAction.Sort -> updateSort(action.sortType)
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
                            // Re-apply filter and sort in case the list updates
                            filteredWords = processList(words, currentState.searchQuery, currentState.sortType)
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
                filteredWords = processList(currentState.fullWordList, query, currentState.sortType)
            )
        }
    }

    private fun updateSort(sortType: SortType) {
        _state.update { currentState ->
            currentState.copy(
                sortType = sortType,
                filteredWords = processList(currentState.fullWordList, currentState.searchQuery, sortType)
            )
        }
    }

    private fun processList(words: List<WordEntity>, query: String, sortType: SortType): List<WordEntity> {
        val filtered = if (query.isBlank()) {
            words
        } else {
            words.filter { it.word.contains(query, ignoreCase = true) }
        }

        return when (sortType) {
            SortType.ALPHABETICAL_ASC -> filtered.sortedBy { it.word.lowercase() }
            SortType.ALPHABETICAL_DESC -> filtered.sortedByDescending { it.word.lowercase() }
            SortType.MASTERY_ASC -> filtered.sortedBy { it.masteryScore }
            SortType.MASTERY_DESC -> filtered.sortedByDescending { it.masteryScore }
            SortType.CREATED_AT_ASC -> filtered.sortedBy { it.createdAt }
            SortType.CREATED_AT_DESC -> filtered.sortedByDescending { it.createdAt }
        }
    }
}
