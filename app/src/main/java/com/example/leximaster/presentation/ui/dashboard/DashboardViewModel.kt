package com.example.leximaster.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leximaster.data.local.entity.WordEntity
import com.example.leximaster.data.remote.dto.WordOfTheDayResponse
import com.example.leximaster.data.remote.error.WordnikError
import com.example.leximaster.data.repository.LexiMasterRepository
import com.example.leximaster.data.repository.MasteryStage
import com.example.leximaster.data.repository.SessionType
import com.example.leximaster.domain.Result
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardState(
    val greetingName: String = "Learner",
    val streakCount: Int = 0,
    val masteredWordsCount: Int = 0,
    val totalWordsCount: Int = 0,
    val noviceCount: Int = 0,
    val competentCount: Int = 0,
    val expertCount: Int = 0,
    val wordOfTheDayState: WordOfTheDayState = WordOfTheDayState.Loading,
)

sealed interface WordOfTheDayState {
    data object Loading : WordOfTheDayState
    data class Success(val wordData: WordOfTheDayResponse) : WordOfTheDayState
    data class Error(val message: String) : WordOfTheDayState
}

sealed interface DashboardAction {
    data object StartNewTest : DashboardAction
    data object StartRandomTest : DashboardAction
    data object RetryWordOfTheDay : DashboardAction
}

sealed interface DashboardEvent {
    data class NavigateToQuiz(val sessionType: SessionType) : DashboardEvent
}

/**
 * Intermediate model carrying all word-derived metrics,
 * computed in-memory from a single Flow<List<WordEntity>>.
 */
private data class WordMetrics(
    val totalWordsCount: Int,
    val masteredWordsCount: Int,
    val noviceCount: Int,
    val competentCount: Int,
    val expertCount: Int,
)


class DashboardViewModel(
    private val repository: LexiMasterRepository
) : ViewModel() {

    // Channel for one-time UI events (navigation)
    private val _eventChannel = Channel<DashboardEvent>()
    val events = _eventChannel.receiveAsFlow()

    private val _wordOfTheDayState = MutableStateFlow<WordOfTheDayState>(WordOfTheDayState.Loading)

    init {
        refreshDashboardData()
        fetchWordOfTheDay()
    }

    /**
     * Clean, encapsulated JIT refresh using your repository wrapper function.
     */
    private fun refreshDashboardData() {
        viewModelScope.launch {
            repository.refreshDashboardData()
        }
    }

    private fun fetchWordOfTheDay() {
        viewModelScope.launch {
            _wordOfTheDayState.value = WordOfTheDayState.Loading
            when (val result = repository.getWordOfTheDay()) {
                is Result.Success -> {
                    _wordOfTheDayState.value = WordOfTheDayState.Success(result.data)
                }
                is Result.Failure -> {
                    _wordOfTheDayState.value = WordOfTheDayState.Error(result.error.message)
                }
            }
        }
    }

    /**
     * Handle UI actions from the Dashboard screen.
     */
    fun onAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.StartNewTest -> {
                viewModelScope.launch {
                    _eventChannel.send(DashboardEvent.NavigateToQuiz(SessionType.NEW_TEST))
                }
            }
            is DashboardAction.StartRandomTest -> {
                viewModelScope.launch {
                    _eventChannel.send(DashboardEvent.NavigateToQuiz(SessionType.RANDOM_TEST))
                }
            }
            is DashboardAction.RetryWordOfTheDay -> {
                fetchWordOfTheDay()
            }
        }
    }

    /**
     * Single-stream word metrics derived from one getAllWords() subscription.
     */
    private val wordMetricsFlow = repository.getAllWords()
        .map { words -> words.toMetrics() }

    /**
     * Unified reactive UI State pipeline.
     */
    val state: StateFlow<DashboardState> = combine(
        wordMetricsFlow,
        repository.observeUserProfile(),
        _wordOfTheDayState
    ) { metrics, profile, wotdState ->
        DashboardState(
            greetingName = profile?.username?.takeIf { it.isNotBlank() } ?: "Learner",
            streakCount = profile?.currentStreak ?: 0,
            totalWordsCount = metrics.totalWordsCount,
            masteredWordsCount = metrics.masteredWordsCount,
            noviceCount = metrics.noviceCount,
            competentCount = metrics.competentCount,
            expertCount = metrics.expertCount,
            wordOfTheDayState = wotdState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState()
    )

    /**
     * Single-pass mapping logic to optimize memory allocation.
     */
    private fun List<WordEntity>.toMetrics(): WordMetrics {
        var novice = 0
        var competent = 0
        var expert = 0
        var mastered = 0

        for (word in this) {
            when (MasteryStage.fromScore(word.masteryScore)) {
                MasteryStage.NOVICE -> novice++
                MasteryStage.COMPETENT -> competent++
                MasteryStage.EXPERT -> expert++
                MasteryStage.MASTERED -> mastered++
            }
        }

        return WordMetrics(
            totalWordsCount = size,
            masteredWordsCount = mastered,
            noviceCount = novice,
            competentCount = competent,
            expertCount = expert,
        )
    }
}