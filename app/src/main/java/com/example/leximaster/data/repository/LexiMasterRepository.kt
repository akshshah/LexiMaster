package com.example.leximaster.data.repository

import androidx.compose.ui.graphics.Color
import com.example.leximaster.data.local.converter.QuestionType
import com.example.leximaster.data.local.converter.ScoreChangeReason
import com.example.leximaster.data.local.dao.ContextDao
import com.example.leximaster.data.local.dao.QuizDao
import com.example.leximaster.data.local.dao.ScoreHistoryDao
import com.example.leximaster.data.local.dao.SynonymDao
import com.example.leximaster.data.local.dao.UserDao
import com.example.leximaster.data.local.dao.WordDao
import com.example.leximaster.data.local.entity.ContextEntity
import com.example.leximaster.data.local.entity.QuizAttemptEntity
import com.example.leximaster.data.local.entity.QuizSessionEntity
import com.example.leximaster.data.local.entity.ScoreHistoryEntity
import com.example.leximaster.data.local.entity.SynonymEntity
import com.example.leximaster.data.local.entity.UserProfileEntity
import com.example.leximaster.data.local.entity.WordEntity
import com.example.leximaster.data.local.model.QuizAttemptWithType
import com.example.leximaster.data.local.model.WordComplete
import com.example.leximaster.data.local.model.WordWithContexts
import com.example.leximaster.data.local.model.getCurrentContext
import com.example.leximaster.data.remote.dto.GeminiWordResponse
import com.example.leximaster.data.remote.error.AiError
import com.example.leximaster.data.remote.service.GeminiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import com.example.leximaster.domain.Result as AppResult

/**
 * Mastery Stage Enum.
 * Maps to mastery_score thresholds:
 * Novice (Stage 1): 0–30
 * Competent (Stage 2): 31–70
 * Expert (Stage 3): 71–99
 * Mastered: 100
 */
enum class MasteryStage(val minScore: Int, val maxScore: Int, val displayName: String, val color: Color) {
    NOVICE(0, 30, "Novice", Color(0xE69E3A34)),
    COMPETENT(31, 70, "Competent", Color(0xE6C97A24)),
    EXPERT(71, 99, "Expert", Color(0xE65F8D6E)),   // Shifted up slightly for better contrast
    MASTERED(100, 100, "Mastered", Color(0xE62A6B54)); // Replaced with a luminous Jade-Pine
    companion object {
        fun fromScore(score: Int): MasteryStage {
            return when {
                score <= 30 -> NOVICE
                score <= 70 -> COMPETENT
                score <= 99 -> EXPERT
                else -> MASTERED
            }
        }
    }
}

/**
 * Context Cycle positions for polysemous words.
 */
enum class ContextCycle(val order: Int, val scoreRange: ClosedRange<Int>) {
    INTRODUCTION(1, 0..33),
    NUANCED(2, 34..66),
    TECHNICAL(3, 67..100);

    companion object {
        fun fromScore(score: Int): ContextCycle {
            return when {
                score <= 33 -> INTRODUCTION
                score <= 66 -> NUANCED
                else -> TECHNICAL
            }
        }
    }
}

/**
 * Quiz Session types.
 */
enum class SessionType(val displayName: String) {
    NEW_TEST("Daily Review"),
    RANDOM_TEST("Category Sprint"),
    REFRESH_QUIZ("Refresher Quiz"),
    CUSTOM("Custom Quiz"),
}

/**
 * Result of a quiz answer with scoring details.
 */
data class QuizResult(
    val isCorrect: Boolean,
    val previousScore: Int,
    val newScore: Int,
    val scoreDelta: Int,
    val questionType: QuestionType,
    val responseTimeMs: Long,
    val masteryStage: MasteryStage,
    val word: WordEntity,
    val context: ContextEntity,
)

/**
 * Result of decay check operation.
 */
data class DecayResult(
    val decayedWords: Int,
    val totalDelta: Int,
    val decayedWordIds: List<Long>,
)

/**
 * Streak calculation result.
 */
data class StreakResult(
    val currentStreak: Int,
    val longestStreak: Int,
    val wasIncremented: Boolean,
    val wasReset: Boolean,
)

/**
 * Main Repository for LexiMaster.
 * Handles all business logic between Room database and app layers.
 */
class LexiMasterRepository(
    private val wordDao: WordDao,
    private val contextDao: ContextDao,
    private val synonymDao: SynonymDao,
    private val userDao: UserDao,
    private val quizDao: QuizDao,
    private val scoreHistoryDao: ScoreHistoryDao,
    private val geminiService: GeminiService,
) {

    private val scorePendingHistory = mutableListOf<ScoreHistoryEntity>()

    // ========== Word Operations ==========

    /**
     * Preview word discovery result for user review.
     * Returns AI-discovered word data without saving to database.
     * User must call createWord() after reviewing and confirming.
     */
    suspend fun previewWordDiscovery(word: String): AppResult<GeminiWordResponse, AiError> =
        withContext(Dispatchers.IO) {
            geminiService.discoverWordData(word)
        }

    /**
     * Get all words with their contexts and synonyms.
     */
    fun getAllWords(): Flow<List<WordEntity>> = wordDao.getAllWords()

    /**
     * Get mastered words (mastery_score = 100).
     */
    fun getMasteredWords(): Flow<List<WordEntity>> = wordDao.getMasteredWords()

    /**
     * Search for words by text.
     */
    fun searchWords(query: String): Flow<List<WordEntity>> = wordDao.searchWords(query)

    /**
     * Get words sorted by mastery stage (Novice → Competent → Expert → Mastered).
     */
    fun getWordsByStage(stage: MasteryStage): Flow<List<WordEntity>> {
        return wordDao.getWordsByScoreRange(stage.minScore, stage.maxScore)
    }

    /**
     * Get a complete word with all related data (contexts + synonyms).
     */
    suspend fun getWordComplete(wordId: Long): WordComplete? = withContext(Dispatchers.IO) {
        val word = wordDao.getWordById(wordId) ?: return@withContext null
        val contexts = contextDao.getContextsByWordId(wordId).first()
        val synonyms = synonymDao.getSynonymsByWordId(wordId).first()

        WordComplete(word, contexts, synonyms)
    }

    /**
     * Get the active context for a word based on its mastery score.
     */
    suspend fun getActiveContext(wordEntity: WordEntity): ContextEntity? = withContext(Dispatchers.IO) {
        val cycle = ContextCycle.fromScore(wordEntity.masteryScore)
        contextDao.getContextByCycleOrder(wordEntity.id, cycle.order)
    }

    /**
     * Create a new word with its contexts and synonyms.
     * Use this after user confirms word discovery preview.
     */
    @Suppress("LongMethod")
    suspend fun createWord(
        wordText: String,
        phonetic: String?,
        contexts: List<Triple<String, String, Int>>, // (meaning, example_usage, cycle_order)
        synonyms: List<String>,
        notes: String? = null,
    ): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val wordId = wordDao.insertWord(
            WordEntity(
                word = wordText,
                phonetic = phonetic,
                notes = notes,
                masteryScore = 0,
                correctAnswers = 0,
                wrongAnswers = 0,
                createdAt = now,
                lastTested = null,
            )
        )

        // Insert contexts
        val contextEntities = contexts.map { (meaning, exampleUsage, cycleOrder) ->
            ContextEntity(
                wordId = wordId,
                meaning = meaning,
                exampleUsage = exampleUsage,
                cycleOrder = cycleOrder,
            )
        }
        contextDao.insertContexts(contextEntities)

        // Insert synonyms (normalized lowercase)
        val synonymEntities = synonyms
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toSet() // Remove duplicates
            .map { SynonymEntity(wordId = wordId, synonymText = it) }

        synonymDao.insertSynonyms(synonymEntities)

        wordId
    }

    /**
     * Delete a word and all its related data (cascade handled by FK).
     */
    suspend fun deleteWord(wordId: Long) = withContext(Dispatchers.IO) {
        wordDao.deleteWord(wordId)
    }

    /**
     * Update notes for a word.
     */
    suspend fun updateNotes(wordId: Long, notes: String) = withContext(Dispatchers.IO) {
        wordDao.updateNotes(wordId, notes)
    }

    // ========== Quiz Operations ==========

    /**
     * Start a quiz session.
     */
    suspend fun startQuizSession(sessionType: SessionType): Long = withContext(Dispatchers.IO) {
        quizDao.createSession(
            QuizSessionEntity(
                startTimestamp = System.currentTimeMillis(),
                endTimestamp = 0,
                totalScoreDelta = 0,
                sessionType = sessionType.displayName,
            )
        )
    }

    /**
     * End a quiz session and update score delta.
     */
    suspend fun endQuizSession(
        sessionId: Long,
        scoreDelta: Int,
    ) = withContext(Dispatchers.IO) {
        quizDao.updateSession(
            id = sessionId,
            endTimestamp = System.currentTimeMillis(),
            scoreDelta = scoreDelta,
        )

        // Flush pending score history
        if (scorePendingHistory.isNotEmpty()) {
            scoreHistoryDao.insertHistories(scorePendingHistory)
            scorePendingHistory.clear()
        }
    }

    /**
     * Record a quiz attempt in a session.
     */
    suspend fun recordQuizAttempt(
        sessionId: Long,
        entity: WordWithContexts,
        questionType: QuestionType,
        userAnswer: Boolean,
        responseTimeMs: Long,
    ): QuizResult = withContext(Dispatchers.IO) {
        val word = entity.word
        val context = getCurrentContext(entity)

        requireNotNull(context) { "Word must have at least one context to be quizzable" }

        // Calculate scoring
        val previousScore = word.masteryScore
        val previousAnswers = Pair(word.correctAnswers, word.wrongAnswers)

        val isCorrect = userAnswer
        val stage = MasteryStage.fromScore(previousScore)

        // Determine points
        val points = when {
            isCorrect && previousScore <= 70 -> WordEntity.POINTS_CORRECT_NOVICE
            isCorrect && previousScore <= 99 -> WordEntity.POINTS_CORRECT_EXPERT
            !isCorrect -> WordEntity.POINTS_WRONG
            else -> 0
        }

        val newScore = (previousScore + points).coerceIn(0, WordEntity.MASTERY_MAX)
        val scoreDelta = newScore - previousScore

        // Update answer counts
        val newCorrectAnswers = if (isCorrect) word.correctAnswers + 1 else word.correctAnswers
        val newWrongAnswers = if (!isCorrect) word.wrongAnswers + 1 else word.wrongAnswers

        // Update word progress
        wordDao.updateProgress(
            id = word.id,
            masteryScore = newScore,
            correctAnswers = newCorrectAnswers,
            wrongAnswers = newWrongAnswers,
            lastTested = System.currentTimeMillis(),
        )

        // Record quiz attempt
        quizDao.insertAttempt(
            QuizAttemptEntity(
                sessionId = sessionId,
                wordId = word.id,
                contextId = context.id,
                questionType = questionType,
                isCorrect = isCorrect,
                responseTimeMs = responseTimeMs,
            )
        )

        // Queue score history (will be batched)
        val reason = if (isCorrect) ScoreChangeReason.QUIZ_CORRECT else ScoreChangeReason.QUIZ_WRONG
        scorePendingHistory.add(
            ScoreHistoryEntity(
                wordId = word.id,
                previousScore = previousScore,
                newScore = newScore,
                delta = scoreDelta,
                reason = reason,
                timestamp = System.currentTimeMillis(),
            )
        )

        QuizResult(
            isCorrect = isCorrect,
            previousScore = previousScore,
            newScore = newScore,
            scoreDelta = scoreDelta,
            questionType = questionType,
            responseTimeMs = responseTimeMs,
            masteryStage = stage,
            word = word.copy(
                masteryScore = newScore,
                correctAnswers = newCorrectAnswers,
                wrongAnswers = newWrongAnswers,
            ),
            context = context,
        )
    }

    /**
     * Get words for a New Test (prioritize low scores).
     */
    suspend fun getWordsForNewTest(): List<WordEntity> = withContext(Dispatchers.IO) {
        wordDao.getWordsForNewTest().first()
    }

    /**
     * Get words for a Random Test (from entire dictionary including mastered).
     */
    suspend fun getWordsForRandomTest(): List<WordEntity> = withContext(Dispatchers.IO) {
        // First, check for decayed words and apply in this session
        checkAndDecayMasteredWords()
        wordDao.getWordsForRandomTest().first()
    }

    /**
     * Get quiz attempts for a word (for word-specific analytics).
     */
    suspend fun getWordQuizHistory(wordId: Long): List<QuizAttemptWithType> =
        withContext(Dispatchers.IO) {
            quizDao.getAttemptsByWordId(wordId)
        }

    // ========== Mastery & Decay ==========

    /**
     * Calculate success rate for a word.
     * Formula: correct_answers / (correct_answers + wrong_answers)
     */
    fun calculateSuccessRate(word: WordEntity): Double {
        val total = word.correctAnswers + word.wrongAnswers
        if (total == 0) return 1.0 // Prioritize words with no history
        return word.correctAnswers.toDouble() / total.toDouble()
    }

    /**
     * Check for mastered words needing decay and apply it.
     * Just-in-time decay check - no extra column needed.
     *
     * @return DecayResult with count of decayed words and total impact
     */
    suspend fun checkAndDecayMasteredWords(): DecayResult = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val masteryScore = WordEntity.MASTERY_MAX
        val threshold = WordEntity.DECAY_THRESHOLD_MS

        // Find words needing decay (score = 100 and inactive > 30 days)
        val wordsNeedingDecay = wordDao.getMasteredWordsNeedingDecay(
            minMasteryScore = masteryScore,
            currentTime = now,
            threshold = threshold,
        )

        if (wordsNeedingDecay.isEmpty()) {
            return@withContext DecayResult(0, 0, emptyList())
        }

        val updates = mutableListOf<Pair<Long, Int>>()
        val decayEvents = mutableListOf<ScoreHistoryEntity>()

        for (word in wordsNeedingDecay) {
            val newScore = maxOf(0, word.masteryScore + WordEntity.POINTS_DECAY)

            updates.add(word.id to newScore)

            decayEvents.add(
                ScoreHistoryEntity(
                    wordId = word.id,
                    previousScore = word.masteryScore,
                    newScore = newScore,
                    delta = WordEntity.POINTS_DECAY,
                    reason = ScoreChangeReason.SYSTEM_DECAY,
                    timestamp = now,
                )
            )
        }

        // Batch update words
        wordDao.updateMasteryScores(updates)

        // Insert decay history
        scoreHistoryDao.insertHistories(decayEvents)

        DecayResult(
            decayedWords = wordsNeedingDecay.size,
            totalDelta = WordEntity.POINTS_DECAY * wordsNeedingDecay.size,
            decayedWordIds = wordsNeedingDecay.map { it.id },
        )
    }

    /**
     * Prune quiz attempts older than 90 days (to be run by background worker).
     */
    suspend fun pruneOldQuizAttempts(): Int = withContext(Dispatchers.IO) {
        val cutoffTimestamp = System.currentTimeMillis() - QuizAttemptEntity.PRUNE_THRESHOLD_MS
        quizDao.pruneOldAttempts(cutoffTimestamp)
    }

    // ========== Streak Management ==========

    /**
     * Update streak based on current activity.
     *
     * Rules:
     * - If < 24 hours since last activity: no change (streak maintained)
     * - If >= 24 hours but < 48 hours: streak += 1
     * - If >= 48 hours: streak = 1 (reset)
     */
    suspend fun updateStreak(): StreakResult = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val profile = userDao.getUserProfileSync(id = UserProfileEntity.PROFILE_ID)

        if (profile == null) {
            return@withContext StreakResult(currentStreak = 1, longestStreak = 1, wasIncremented = false, wasReset = false)
        }

        val timeSinceLastActivity = now - profile.lastActiveDate
        val streakThreshold = UserProfileEntity.STREAK_THRESHOLD_MS
        val resetThreshold = UserProfileEntity.STREAK_RESET_THRESHOLD_MS

        return@withContext when {
            timeSinceLastActivity < streakThreshold -> {
                // Within 24 hours - streak maintained
                StreakResult(
                    currentStreak = profile.currentStreak,
                    longestStreak = profile.longestStreak,
                    wasIncremented = false,
                    wasReset = false,
                )
            }

            timeSinceLastActivity < resetThreshold -> {
                // 24-48 hours - increment streak
                val newStreak = profile.currentStreak + 1
                val newLongestStreak = maxOf(profile.longestStreak, newStreak)

                userDao.incrementStreak(
                    id = profile.id,
                    currentTimestamp = now,
                    previousLongestStreak = profile.longestStreak,
                    streakThreshold = UserProfileEntity.STREAK_THRESHOLD_MS,
                    resetThreshold = UserProfileEntity.STREAK_RESET_THRESHOLD_MS,
                )

                StreakResult(
                    currentStreak = newStreak,
                    longestStreak = newLongestStreak,
                    wasIncremented = true,
                    wasReset = false,
                )
            }

            else -> {
                // 48+ hours - reset streak
                userDao.updateLastActiveDate(id = profile.id, lastActiveDate = now)

                StreakResult(
                    currentStreak = 1,
                    longestStreak = profile.longestStreak,
                    wasIncremented = false,
                    wasReset = true,
                )
            }
        }
    }

    // ========== Canvas (Analytics) ==========

    /**
     * Decrement lastActiveDate by given hours (for simulation/debug).
     */
    suspend fun decrementLastActiveBy(hours: Int) = withContext(Dispatchers.IO) {
        val profile = userDao.getUserProfileSync(id = UserProfileEntity.PROFILE_ID) ?: return@withContext
        val newDate = profile.lastActiveDate - (hours * 60L * 60L * 1000L)
        userDao.updateLastActiveDate(id = profile.id, lastActiveDate = newDate)
    }

    suspend fun createUserProfile(username: String) = withContext(Dispatchers.IO) {
        userDao.createProfile(
            UserProfileEntity(
               username = username
            )
        )
    }

    suspend fun updateUsername(id: Int, username: String) = withContext(Dispatchers.IO) {
        userDao.updateUsername(id, username)
    }


    /**
     * Observe user profile as a continuous Flow stream.
     */
    fun observeUserProfile(): Flow<UserProfileEntity?> =
        userDao.getUserProfile(UserProfileEntity.PROFILE_ID)

    /**
     * Trigger JustInTime computations for fresh dashboard state.
     */
    suspend fun refreshDashboardData() = withContext(Dispatchers.IO) {
        updateStreak()
        checkAndDecayMasteredWords()
    }
}
