package com.example.leximaster.presentation.ui.quiz

import android.os.Parcelable
import com.example.leximaster.data.local.converter.QuestionType
import com.example.leximaster.data.local.entity.ContextEntity
import com.example.leximaster.data.local.entity.WordEntity
import com.example.leximaster.data.repository.MasteryStage
import com.example.leximaster.data.repository.SessionType
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * UI State for the Quiz screen.
 * Follows UDF principles with immutable state updates.
 */
@Parcelize
data class QuizUiState(
    // Session information
    val sessionId: Long? = null,
    val sessionType: SessionType? = null,
    // Current question data
    val currentWord: @RawValue WordEntity? = null,
    val activeContext: @RawValue ContextEntity? = null,
    val currentQuestionType: QuestionType? = null,
    val question: String? = null,
    // Multiple choice options (4 shuffled options: 1 correct + 3 distractors)
    val options: List<String> = emptyList(),
    // Selected option for highlighting
    val selectedOption: String? = null,
    // Progress tracking
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = TOTAL_QUESTIONS_PER_SESSION,
    // Score tracking
    val currentScore: Int = 0,
    val correctAnswers: Int = 0,
    // Feedback state
    val feedback: @RawValue QuizFeedback? = null,
    // UI state flags
    val isAnswerLocked: Boolean = false,
    val isLoading: Boolean = true,
    val isCompleting: Boolean = false,
    val isCompleted: Boolean = false,
    // Error state
    val errorMessage: String? = null,
) : Parcelable {

    /**
     * Check if there are more questions remaining.
     */
    val hasMoreQuestions: Boolean
        get() = currentQuestionIndex < totalQuestions - 1

    /**
     * Check if this is the last question.
     */
    val isLastQuestion: Boolean
        get() = currentQuestionIndex == totalQuestions - 1

    /**
     * Progress as a fraction (0.0 to 1.0).
     */
    val progressFraction: Float
        get() = if (totalQuestions > 0) {
            (currentQuestionIndex + 1).toFloat() / totalQuestions.toFloat()
        } else {
            0f
        }

    companion object {
        const val TOTAL_QUESTIONS_PER_SESSION = 10
    }
}

/**
 * Feedback data shown after answering a question.
 */
@Parcelize
data class QuizFeedback(
    val isCorrect: Boolean,
    val correctAnswer: String,
    val previousScore: Int,
    val newScore: Int,
    val scoreDelta: Int,
    val masteryStage: @RawValue MasteryStage,
    val word: @RawValue WordEntity,
    val context: @RawValue ContextEntity,
) : Parcelable

/**
 * Sealed interface for all user actions in the Quiz screen.
 * Following UDF pattern for event handling.
 */
sealed interface QuizAction {
    /**
     * User submits an answer.
     */
    data class OnSubmitAnswer(val selectedWord: String) : QuizAction // Changed from Boolean

    /**
     * User requests the next question.
     */
    data object OnNextQuestion : QuizAction

    /**
     * User quits the quiz session.
     */
    data object OnQuitQuiz : QuizAction
}

/**
 * Sealed interface for one-time UI effects.
 * Used for navigation and snackbar events.
 */
sealed interface QuizEffect {
    /**
     * Navigate back to the dashboard after quiz completion.
     */
    data object NavigateToDashboard : QuizEffect

    /**
     * Show a snackbar message.
     */
    data class ShowSnackbar(val message: String) : QuizEffect
}
