package com.example.leximaster.presentation.word

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leximaster.data.repository.LexiMasterRepository
import com.example.leximaster.domain.Result
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class WordDiscoveryViewModel(
    private val repository: LexiMasterRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(restoreState())
    val state: StateFlow<WordDiscoveryState> = _state.asStateFlow()

    private val _event = Channel<WordDiscoveryEvent>()
    val event: Flow<WordDiscoveryEvent> = _event.receiveAsFlow()

    fun onAction(action: WordDiscoveryAction) {
        when (action) {
            is WordDiscoveryAction.UpdateSearchQuery -> updateSearchQuery(action.query)
            is WordDiscoveryAction.SubmitSearch -> submitSearch()
            is WordDiscoveryAction.RemoveSynonym -> removeSynonym(action.synonym)
            is WordDiscoveryAction.ConfirmAndSave -> confirmAndSave()
            is WordDiscoveryAction.ClearError -> clearError()
        }
    }

    private fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(
            searchQuery = query,
            previewData = null,
            editedSynonyms = null,
            errorMessage = null,
        )
    }

    private fun submitSearch() {
        val query = _state.value.searchQuery.trim()
        if (query.isEmpty()) {
            _event.trySend(WordDiscoveryEvent.ShowSnackbar("Please enter a word to search"))
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSearching = true)

            when (val result = repository.previewWordDiscovery(query)) {
                is Result.Success -> {
                    val previewData = result.data
                    _state.value = _state.value.copy(
                        isSearching = false,
                        previewData = previewData,
                        editedSynonyms = previewData.synonyms.toList(),
                        errorMessage = null,
                    )
                    saveState()
                }
                is Result.Failure -> {
                    _state.value = _state.value.copy(
                        isSearching = false,
                        errorMessage = mapErrorToMessage(result.error),
                    )
                }
            }
        }
    }

    private fun removeSynonym(synonym: String) {
        val currentSynonyms = _state.value.editedSynonyms ?: emptyList()
        _state.value = _state.value.copy(
            editedSynonyms = currentSynonyms - synonym,
        )
    }

    private fun confirmAndSave() {
        val previewData = _state.value.previewData
        if (previewData == null) {
            _event.trySend(WordDiscoveryEvent.ShowSnackbar("No word preview to save"))
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)

            // Check if word already exists
            val existingWords = repository.searchWords(_state.value.searchQuery).first()
            existingWords.firstOrNull { it.word.equals(_state.value.searchQuery, ignoreCase = true) }?.also { existing ->
                _event.trySend(WordDiscoveryEvent.WordAlreadyExists(existing.word))
                _state.value = _state.value.copy(isSaving = false)
                return@launch
            }

            // Save the word with contexts and edited synonyms
            try {
                val contexts = previewData.contexts.map {
                    Triple(it.meaning, it.exampleUsage, it.cycleOrder)
                }
                val synonyms = _state.value.editedSynonyms ?: emptyList()
                repository.createWord(
                    wordText = _state.value.searchQuery,
                    phonetic = previewData.phonetic,
                    contexts = contexts,
                    synonyms = synonyms,
                    notes = null,
                )

                _event.trySend(WordDiscoveryEvent.WordSavedSuccessfully)
                _state.value = WordDiscoveryState()
                savedStateHandle.remove(KEY_STATE)
            } catch (e: Exception) {
                _event.trySend(WordDiscoveryEvent.ShowSnackbar("Failed to save word: ${e.message}"))
                _state.value = _state.value.copy(isSaving = false)
            }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    private fun mapErrorToMessage(error: Any): String {
        return when (error) {
            else -> "Failed to load word data. Please try again."
        }
    }

    private fun saveState() {
        savedStateHandle[KEY_STATE] = _state.value
    }

    private fun restoreState(): WordDiscoveryState {
        return savedStateHandle.get<WordDiscoveryState>(KEY_STATE) ?: WordDiscoveryState()
    }

    companion object {
        private const val KEY_STATE = "word_discovery_state"
    }
}
