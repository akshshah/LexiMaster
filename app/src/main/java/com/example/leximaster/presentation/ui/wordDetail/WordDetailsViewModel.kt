package com.example.leximaster.presentation.ui.wordDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leximaster.data.local.entity.ContextEntity
import com.example.leximaster.data.local.entity.WordEntity
import com.example.leximaster.data.local.entity.SynonymEntity
import com.example.leximaster.data.local.model.WordComplete
import com.example.leximaster.data.repository.ContextCycle
import com.example.leximaster.data.repository.LexiMasterRepository
import com.example.leximaster.data.repository.MasteryStage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─── State ───────────────────────────────────────────────────────────────────

data class WordDetailsState(
    val isLoading: Boolean = true,
    val word: WordEntity? = null,
    val contexts: List<ContextEntity> = emptyList(),
    val synonyms: List<SynonymEntity> = emptyList(),
    val activeContextOrder: Int = ContextEntity.CYCLE_INTRODUCTION,
    val masteryStage: MasteryStage = MasteryStage.NOVICE,
    val successRate: Float = 0f,
    val isEditingNotes: Boolean = false,
    val notesInput: String = "",
    val showDeleteDialog: Boolean = false,
    val error: String? = null,
)

// ─── Actions ─────────────────────────────────────────────────────────────────

sealed interface WordDetailsAction {
    data object OnBackClick : WordDetailsAction
    data object OnDeleteClick : WordDetailsAction
    data object OnConfirmDelete : WordDetailsAction
    data object OnDismissDeleteDialog : WordDetailsAction
    data object OnEditNotesClick : WordDetailsAction
    data object OnCancelEditNotes : WordDetailsAction
    data object OnSaveNotes : WordDetailsAction
    data class OnNotesChanged(val notes: String) : WordDetailsAction
}

// ─── Events ──────────────────────────────────────────────────────────────────

sealed interface WordDetailsEvent {
    data object NavigateBack : WordDetailsEvent
}

// ─── ViewModel ───────────────────────────────────────────────────────────────

class WordDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: LexiMasterRepository,
) : ViewModel() {

    private val wordId: Long = checkNotNull(savedStateHandle["wordId"])

    private val _state = MutableStateFlow(WordDetailsState())
    val state: StateFlow<WordDetailsState> = _state.asStateFlow()

    private val _events = Channel<WordDetailsEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadWord()
    }

    fun onAction(action: WordDetailsAction) {
        when (action) {
            WordDetailsAction.OnBackClick -> sendEvent(WordDetailsEvent.NavigateBack)
            WordDetailsAction.OnDeleteClick -> _state.update { it.copy(showDeleteDialog = true) }
            WordDetailsAction.OnDismissDeleteDialog -> _state.update { it.copy(showDeleteDialog = false) }
            WordDetailsAction.OnConfirmDelete -> deleteWord()
            WordDetailsAction.OnEditNotesClick -> {
                _state.update {
                    it.copy(
                        isEditingNotes = true,
                        notesInput = it.word?.notes.orEmpty(),
                    )
                }
            }
            WordDetailsAction.OnCancelEditNotes -> {
                _state.update { it.copy(isEditingNotes = false) }
            }
            WordDetailsAction.OnSaveNotes -> saveNotes()
            is WordDetailsAction.OnNotesChanged -> {
                _state.update { it.copy(notesInput = action.notes) }
            }
        }
    }

    // ─── Private ─────────────────────────────────────────────────────────────

    private fun loadWord() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val wordComplete = repository.getWordComplete(wordId)
                if (wordComplete == null) {
                    _state.update { it.copy(isLoading = false, error = "Word not found.") }
                    return@launch
                }
                _state.update { mapWordComplete(wordComplete) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load word.")
                }
            }
        }
    }

    private fun mapWordComplete(wordComplete: WordComplete): WordDetailsState {
        val word = wordComplete.word
        val total = word.correctAnswers + word.wrongAnswers
        val successRate = if (total == 0) 0f else word.correctAnswers.toFloat() / total.toFloat()
        val activeOrder = ContextCycle.fromScore(word.masteryScore).order

        return WordDetailsState(
            isLoading = false,
            word = word,
            contexts = wordComplete.contexts.sortedBy { it.cycleOrder },
            synonyms = wordComplete.synonyms,
            activeContextOrder = activeOrder,
            masteryStage = MasteryStage.fromScore(word.masteryScore),
            successRate = successRate,
            notesInput = word.notes.orEmpty(),
        )
    }

    private fun deleteWord() {
        viewModelScope.launch {
            try {
                repository.deleteWord(wordId)
                sendEvent(WordDetailsEvent.NavigateBack)
            } catch (e: Exception) {
                _state.update {
                    it.copy(showDeleteDialog = false, error = "Failed to delete word.")
                }
            }
        }
    }

    private fun saveNotes() {
        viewModelScope.launch {
            val notes = _state.value.notesInput
            try {
                repository.updateNotes(wordId, notes)
                // Reflect the change locally without a full reload
                _state.update { current ->
                    current.copy(
                        isEditingNotes = false,
                        word = current.word?.copy(notes = notes),
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to save notes.") }
            }
        }
    }

    private fun sendEvent(event: WordDetailsEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}
