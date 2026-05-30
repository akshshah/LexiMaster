package com.example.leximaster.presentation.ui.quiz

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leximaster.data.local.converter.QuestionType
import com.example.leximaster.data.local.entity.ContextEntity
import com.example.leximaster.data.local.entity.WordEntity
import com.example.leximaster.data.local.model.WordWithContexts
import com.example.leximaster.data.remote.service.GeminiService
import com.example.leximaster.data.repository.ContextCycle
import com.example.leximaster.data.repository.LexiMasterRepository
import com.example.leximaster.data.repository.QuizResult
import com.example.leximaster.data.repository.SessionType
import com.example.leximaster.domain.Result
import com.example.leximaster.presentation.navigation.QuizRoute
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val TAG = "QuizViewModel"

/**
 * Internal data structure for a pre-compiled quiz question.
 * Contains all data needed to display and evaluate a question without network calls.
 */
private data class PrecompiledQuestion(
    val word: WordEntity,
    val activeContext: ContextEntity,
    val questionType: QuestionType,
    val options: List<String>, // Shuffled: 1 correct answer + 3 distractors
    val correctAnswer: String, // The correct answer (word for SYNONYM, meaning for RECOGNITION/RECALL)
)

/**
 * ViewModel for the Quiz screen following UDF architecture.
 *
 * Responsibilities:
 * - Manages quiz session lifecycle (start, progress, complete)
 * - Handles user answer submissions with duplicate prevention
 * - Tracks score and mastery progress
 * - Coordinates with repository for all quiz operations
 * - Pre-generates distractors concurrently for smooth quiz experience
 *
 * @param repository The main data repository for quiz operations
 * @param geminiService The AI service for generating distractors
 * @param savedStateHandle For retrieving navigation arguments (sessionType)
 */
class QuizViewModel(
    private val repository: LexiMasterRepository,
    private val geminiService: GeminiService,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private val _effect = Channel<QuizEffect>()
    val effect: Flow<QuizEffect> = _effect.receiveAsFlow()

    // Internal state for quiz session management
    private var wordPool: List<WordEntity> = emptyList()
    private var wordContextsMap: Map<Long, WordWithContexts> = emptyMap()
    private var answerStartTime: Long = 0

    // Pre-compiled questions cache - eliminates network calls during quiz
    private var precompiledQuestions: Map<Int, PrecompiledQuestion> = emptyMap()

    // Error tracking for distractor generation failures
    private var distractorGenerationFailures: Int = 0

    init {
        // Extract sessionType from navigation arguments
        val sessionType = savedStateHandle.get<SessionType>(QuizRoute::sessionType.name)
        if (sessionType != null) {
            initializeQuizSession(sessionType)
        } else {
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "Invalid session type",
            )
        }
    }

    /**
     * Handle user events from the UI.
     * Single entry point for all user interactions.
     */
    fun onEvent(event: QuizEvent) {
        when (event) {
            is QuizEvent.OnSubmitAnswer -> handleSubmitAnswer(event.selectedWord)
            is QuizEvent.OnNextQuestion -> handleNextQuestion()
            is QuizEvent.OnQuitQuiz -> handleQuitQuiz()
        }
    }

    /**
     * Initialize the quiz session.
     * - Creates a new session in the database
     * - Fetches the appropriate word pool based on session type
     * - Concurrently generates distractors for all questions
     * - Loads the first question
     */
    private fun initializeQuizSession(sessionType: SessionType) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                sessionType = sessionType,
                errorMessage = null,
            )

            try {
                // Start quiz session in repository
                val sessionId = repository.startQuizSession(sessionType)

                // Fetch word pool based on session type
                val words = fetchWordPool(sessionType)

                if (words.isEmpty()) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "No words available for this quiz. Add some words to your library first.",
                    )
                    return@launch
                }

                // Limit to configured number of questions
                val selectedWords = words.take(QuizUiState.TOTAL_QUESTIONS_PER_SESSION)

                // Load complete structured wrappers from our repository
                val contextMap = mutableMapOf<Long, WordWithContexts>()
                for (word in selectedWords) {
                    val completeData = repository.getWordComplete(word.id)
                    if (completeData != null) {
                        contextMap[word.id] = WordWithContexts(completeData.word, completeData.contexts)
                    }
                }

                // Store in internal state
                wordPool = selectedWords
                wordContextsMap = contextMap

                // Reset failure counter
                distractorGenerationFailures = 0

                // Concurrently generate distractors for all questions
                val questionsMap = generateDistractorsConcurrently(selectedWords, contextMap)

                if (questionsMap.isEmpty()) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to generate quiz questions. Please try again.",
                    )
                    return@launch
                }

                // Log if we had failures during generation
                if (distractorGenerationFailures > 0) {
                    Log.w(TAG, "Distractor generation had $distractorGenerationFailures failures during session initialization")
                }

                precompiledQuestions = questionsMap

                // Load first question
                loadQuestion(
                    sessionId = sessionId,
                    questionIndex = 0,
                    totalQuestionsCount = questionsMap.size
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to start quiz: ${e.message}",
                )
            }
        }
    }

    /**
     * Generate distractors concurrently for all quiz questions.
     * Uses async/awaitAll to parallelize API calls during initialization.
     * This ensures zero network latency when switching between questions.
     */
    private suspend fun generateDistractorsConcurrently(
        words: List<WordEntity>,
        contextMap: Map<Long, WordWithContexts>
    ): Map<Int, PrecompiledQuestion> {
        val questionsMap = mutableMapOf<Int, PrecompiledQuestion>()

        // Create deferred tasks for each word
        val deferredQuestions = words.map { word ->
            viewModelScope.async {
                try {
                    // Get word with contexts from the pre-loaded contextMap (no DB call)
                    val wordWithContexts = contextMap[word.id]
                    if (wordWithContexts == null) {
                        Log.w(TAG, "No context data found for word: ${word.word}")
                        distractorGenerationFailures++
                        return@async null
                    }

                    // Resolve active context locally using the same logic as repository
                    // This eliminates redundant DB calls during concurrent generation
                    val activeContext = resolveActiveContext(wordWithContexts)
                    if (activeContext == null) {
                        Log.w(TAG, "No active context found for word: ${word.word}")
                        distractorGenerationFailures++
                        return@async null
                    }

                    // Determine question type FIRST before generating distractors
                    val questionType = QuestionType.entries.random()

                    // Log for debugging
                    Log.d(TAG, "Generating question for '${word.word}' with type: $questionType")

                    // Generate distractors via Gemini with question-type-aware prompt
                    val distractors = generateDistractorsForWord(
                        word = word.word,
                        correctMeaning = activeContext.meaning,
                        exampleContext = activeContext.exampleUsage,
                        questionType = questionType
                    )

                    if (distractors == null) {
                        distractorGenerationFailures++
                        return@async null
                    }

                    // Build options based on question type
                    val (options, correctAnswer) = buildOptionsForQuestionType(
                        word = word,
                        activeContext = activeContext,
                        questionType = questionType,
                        distractors = distractors
                    )

                    PrecompiledQuestion(
                        word = word,
                        activeContext = activeContext,
                        questionType = questionType,
                        options = options,
                        correctAnswer = correctAnswer
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error generating question for word: ${word.word}", e)
                    distractorGenerationFailures++
                    null
                }
            }
        }

        // Wait for all concurrent operations to complete
        val results = deferredQuestions.awaitAll()

        // Build the questions map, skipping any failed generations
        results.forEach { question ->
            if (question != null) {
                questionsMap[questionsMap.size] = question
            }
        }

        return questionsMap
    }

    /**
     * Resolve the active context for a word based on its mastery score.
     * This mirrors the repository logic but uses pre-loaded context data
     * to eliminate redundant database calls during concurrent generation.
     *
     * @param wordWithContexts The word with its loaded contexts
     * @return The active context based on the word's mastery score, or null if not found
     */
    private fun resolveActiveContext(wordWithContexts: WordWithContexts): ContextEntity? {
        val word = wordWithContexts.word
        val contexts = wordWithContexts.contexts

        if (contexts.isEmpty()) {
            return null
        }

        // Determine the cycle order based on mastery score (same logic as ContextCycle.fromScore)
        val cycleOrder = when {
            word.masteryScore <= 33 -> 1  // INTRODUCTION
            word.masteryScore <= 66 -> 2  // NUANCED
            else -> 3                      // TECHNICAL
        }

        // Find the context matching the cycle order
        return contexts.find { it.cycleOrder == cycleOrder }
    }

    /**
     * Build options and correct answer based on question type.
     * - SYNONYM: Options are words, correct answer is the word itself
     * - RECOGNITION/RECALL: Options are meanings/definitions, correct answer is the meaning
     */
    private fun buildOptionsForQuestionType(
        word: WordEntity,
        activeContext: ContextEntity,
        questionType: QuestionType,
        distractors: List<String>
    ): Pair<List<String>, String> {
        return when (questionType) {
            QuestionType.SYNONYM -> {
                // For synonym questions: options are words
                val options = (listOf(word.word) + distractors).shuffled()
                Pair(options, word.word)
            }
            QuestionType.RECOGNITION,
            QuestionType.RECALL -> {
                // For recognition/recall: options are meanings/definitions
                val options = (listOf(activeContext.meaning) + distractors).shuffled()
                Pair(options, activeContext.meaning)
            }
        }
    }

    /**
     * Generate distractors for a single word using GeminiService.
     * Includes strict sanitization and validation.
     * Returns 3 validated distractors or null on failure.
     */
    private suspend fun generateDistractorsForWord(
        word: String,
        correctMeaning: String,
        exampleContext: String,
        questionType: QuestionType
    ): List<String>? {
        return when (val result = geminiService.generateDistractors(
            word = word,
            correctMeaning = correctMeaning,
            exampleContext = exampleContext,
            questionType = questionType
        )) {
            is Result.Success -> {
                val rawDistractors = result.data

                // Sanitization: Filter out any distractor that matches the correct answer
                val sanitizedDistractors = rawDistractors
                    .filter { distractor ->
                        val normalizedDistractor = distractor.trim().lowercase()
                        val normalizedWord = word.trim().lowercase()
                        val normalizedMeaning = correctMeaning.trim().lowercase()

                        // Ensure distractor doesn't match the word or correct meaning
                        normalizedDistractor != normalizedWord &&
                        normalizedDistractor != normalizedMeaning &&
                        normalizedDistractor.isNotBlank()
                    }
                    .distinctBy { it.trim().lowercase() } // Remove duplicates

                // Validate we have exactly 3 distinct distractors
                if (sanitizedDistractors.size >= 3) {
                    sanitizedDistractors.take(3)
                } else {
                    Log.w(
                        TAG,
                        "Insufficient valid distractors for word '$word': " +
                        "got ${sanitizedDistractors.size}, needed 3. " +
                        "Raw response: $rawDistractors"
                    )
                    null
                }
            }
            is Result.Failure -> {
                Log.e(TAG, "Failed to generate distractors for word '$word': ${result.error}")
                null
            }
        }
    }

    /**
     * Fetch the word pool based on session type.
     */
    private suspend fun fetchWordPool(sessionType: SessionType): List<WordEntity> {
        return when (sessionType) {
            SessionType.NEW_TEST -> repository.getWordsForNewTest()
            SessionType.RANDOM_TEST -> repository.getWordsForRandomTest()
            SessionType.REFRESH_QUIZ -> repository.getWordsForNewTest()
            SessionType.CUSTOM -> repository.getWordsForRandomTest()
        }
    }

    /**
     * Load a question at the specified index.
     * Fetches pre-compiled question data instantly from cache.
     */
    private fun loadQuestion(
        sessionId: Long,
        questionIndex: Int,
        totalQuestionsCount: Int = _state.value.totalQuestions
    ) {
        if (questionIndex >= precompiledQuestions.size) {
            // No more questions - complete the quiz
            completeQuiz()
            return
        }

        val question = precompiledQuestions[questionIndex]
        if (question == null) {
            // Skip invalid question
            loadQuestion(sessionId, questionIndex + 1, totalQuestionsCount)
            return
        }

        // Log warning for question types that might need special UI handling
        // This helps identify if UI needs updates for new question types
        Log.d(TAG, "Loading question ${questionIndex + 1}: type=${question.questionType}, word=${question.word.word}")

        // Record answer start time for response time tracking
        answerStartTime = System.currentTimeMillis()

        _state.value = _state.value.copy(
            sessionId = sessionId,
            currentWord = question.word,
            activeContext = question.activeContext,
            currentQuestionType = question.questionType,
            options = question.options,
            currentQuestionIndex = questionIndex,
            totalQuestions = totalQuestionsCount,
            isAnswerLocked = false,
            feedback = null,
            isLoading = false,
            errorMessage = null,
        )
    }

    /**
     * Handle answer submission.
     * Prevents duplicate submissions using isAnswerLocked flag.
     * Validates against pre-compiled correct answer.
     * Records the attempt and updates score/mastery progress.
     */
    private fun handleSubmitAnswer(selectedWord: String) {
        val currentState = _state.value

        // Prevent duplicate submissions
        if (currentState.isAnswerLocked) {
            return
        }

        // Validate required state
        val sessionId = currentState.sessionId
        val questionIndex = currentState.currentQuestionIndex
        val question = precompiledQuestions[questionIndex]

        if (sessionId == null || question == null) {
            _effect.trySend(QuizEffect.ShowSnackbar("Unable to submit answer. Please try again."))
            return
        }

        // Lock answer to prevent duplicate submissions
        _state.value = currentState.copy(isAnswerLocked = true)

        viewModelScope.launch {
            try {
                // Calculate response time
                val responseTimeMs = System.currentTimeMillis() - answerStartTime
                val wordWithContexts = wordContextsMap[question.word.id] ?: return@launch

                // Validate answer against pre-compiled correct answer
                val isCorrectAnswer = selectedWord.trim().equals(
                    question.correctAnswer.trim(),
                    ignoreCase = true
                )

                // Record the quiz attempt
                val quizResult: QuizResult = repository.recordQuizAttempt(
                    sessionId = sessionId,
                    entity = wordWithContexts,
                    questionType = question.questionType,
                    userAnswer = isCorrectAnswer,
                    responseTimeMs = responseTimeMs,
                )

                // Update state with feedback
                _state.value = _state.value.copy(
                    feedback = QuizFeedback(
                        isCorrect = quizResult.isCorrect,
                        previousScore = quizResult.previousScore,
                        newScore = quizResult.newScore,
                        scoreDelta = quizResult.scoreDelta,
                        masteryStage = quizResult.masteryStage,
                        word = quizResult.word,
                        context = quizResult.context,
                    ),
                    currentScore = _state.value.currentScore + quizResult.scoreDelta,
                    correctAnswers = if (quizResult.isCorrect) {
                        _state.value.correctAnswers + 1
                    } else {
                        _state.value.correctAnswers
                    },
                )

                // Update the word in the pool with new mastery score
                wordPool = wordPool.map { existingWord ->
                    if (existingWord.id == question.word.id) {
                        quizResult.word
                    } else {
                        existingWord
                    }
                }
                precompiledQuestions = precompiledQuestions.mapValues { entry ->
                    if (entry.value.word.id == question.word.id) {
                        entry.value.copy(word = quizResult.word)
                    } else {
                        entry.value
                    }
                }
            } catch (e: Exception) {
                _effect.trySend(QuizEffect.ShowSnackbar("Failed to record answer: ${e.message}"))
                _state.value = _state.value.copy(isAnswerLocked = false)
            }
        }
    }

    /**
     * Handle advancing to the next question.
     * Loads the next pre-compiled question instantly.
     */
    private fun handleNextQuestion() {
        val currentState = _state.value
        val sessionId = currentState.sessionId

        if (sessionId == null) {
            _effect.trySend(QuizEffect.ShowSnackbar("Session not found."))
            return
        }

        if (!currentState.hasMoreQuestions) {
            // This is the last question - complete the quiz
            completeQuiz()
            return
        }

        // Load next question (no suspend call needed - data is pre-compiled)
        loadQuestion(sessionId, currentState.currentQuestionIndex + 1)
    }

    /**
     * Handle quitting the quiz session.
     * Ends the session in the repository and navigates back.
     */
    private fun handleQuitQuiz() {
        val sessionId = _state.value.sessionId

        viewModelScope.launch {
            _state.value = _state.value.copy(isCompleting = true)

            try {
                if (sessionId != null) {
                    // End the session with current score delta
                    repository.endQuizSession(
                        sessionId = sessionId,
                        scoreDelta = _state.value.currentScore,
                    )
                }
            } catch (e: Exception) {
                _effect.trySend(QuizEffect.ShowSnackbar("${e.message}"))
            }
            _effect.trySend(QuizEffect.NavigateToDashboard)
        }
    }

    /**
     * Complete the quiz session.
     * Ends the session in the repository and emits navigation effect.
     */
    private fun completeQuiz() {
        val sessionId = _state.value.sessionId

        viewModelScope.launch {
            _state.value = _state.value.copy(isCompleting = true)

            try {
                if (sessionId != null) {
                    // End the session with final score delta
                    repository.endQuizSession(
                        sessionId = sessionId,
                        scoreDelta = _state.value.currentScore,
                    )
                }

                // Update completion state
                _state.value = _state.value.copy(
                    isCompleted = true,
                    isCompleting = false,
                )

                // Emit navigation effect
                _effect.trySend(QuizEffect.NavigateToDashboard)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isCompleted = true,
                    isCompleting = false,
                )
                _effect.trySend(QuizEffect.ShowSnackbar("Quiz completed but failed to save session."))
                _effect.trySend(QuizEffect.NavigateToDashboard)
            }
        }
    }
}
