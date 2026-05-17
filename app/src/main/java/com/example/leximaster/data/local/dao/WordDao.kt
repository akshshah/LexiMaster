package com.example.leximaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.leximaster.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for WordEntity operations.
 */
@Dao
interface WordDao {

    /**
     * Get all words ordered by creation date (newest first).
     */
    @Query("SELECT * FROM words ORDER BY created_at DESC")
    fun getAllWords(): Flow<List<WordEntity>>

    /**
     * Get mastered words (mastery_score = 100).
     */
    @Query("SELECT * FROM words WHERE mastery_score = 100 ORDER BY mastery_score DESC, created_at DESC")
    fun getMasteredWords(): Flow<List<WordEntity>>

    /**
     * Search for words by text (LIKE query).
     */
    @Query("SELECT * FROM words WHERE word LIKE '%' || :query || '%' ORDER BY created_at DESC LIMIT 20")
    fun searchWords(query: String): Flow<List<WordEntity>>

    /**
     * Get a single word by its ID.
     */
    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): WordEntity?

    /**
     * Get a word by the word text.
     */
    @Query("SELECT * FROM words WHERE word = :word LIMIT 1")
    suspend fun getWordByText(word: String): WordEntity?

    /**
     * Get words with mastery score in a specific stage range.
     * Useful for filtering by Novice, Competent, Expert stages.
     */
    @Query("SELECT * FROM words WHERE mastery_score BETWEEN :minScore AND :maxScore ORDER BY created_at DESC")
    fun getWordsByScoreRange(minScore: Int, maxScore: Int): Flow<List<WordEntity>>

    /**
     * Get words with low scores for New Test (0-40 range).
     */
    @Query("SELECT * FROM words WHERE mastery_score BETWEEN 0 AND 40 ORDER BY RANDOM() LIMIT 10")
    fun getWordsForNewTest(): Flow<List<WordEntity>>

    /**
     * Get random words for Random Test from entire dictionary.
     */
    @Query("SELECT * FROM words WHERE mastery_score < 100 ORDER BY RANDOM() LIMIT 10")
    fun getWordsForRandomTest(): Flow<List<WordEntity>>

    /**
     * Get words for decay check - mastered words where last_tested > 30 days ago.
     */
    @Query("""
        SELECT * FROM words
        WHERE mastery_score = :masteryScore
        AND (:currentTime - IFNULL(last_tested, 0)) > :threshold
        LIMIT 50
    """)
    suspend fun getWordsForDecay(masteryScore: Int, currentTime: Long, threshold: Long): List<WordEntity>

    /**
     * Get mastered words for decay check.
     */
    @Query("""
        SELECT * FROM words
        WHERE mastery_score >= :minMasteryScore
        AND (:currentTime - IFNULL(last_tested, 0)) > :threshold
    """)
    suspend fun getMasteredWordsNeedingDecay(minMasteryScore: Int, currentTime: Long, threshold: Long): List<WordEntity>

    /**
     * Insert a word. Returns the inserted row ID.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWord(word: WordEntity): Long

    /**
     * Update progress for a word (mastery_score, correct_answers, wrong_answers, last_tested).
     */
    @Query("""
        UPDATE words
        SET mastery_score = :masteryScore,
            correct_answers = :correctAnswers,
            wrong_answers = :wrongAnswers,
            last_tested = :lastTested
        WHERE id =:id
    """)
    suspend fun updateProgress(
        id: Long,
        masteryScore: Int,
        correctAnswers: Int,
        wrongAnswers: Int,
        lastTested: Long,
    )

    /**
     * Update mastery score for a word.
     */
    @Query("UPDATE words SET mastery_score = :masteryScore WHERE id = :id")
    suspend fun updateMasteryScore(id: Long, masteryScore: Int)

    /**
     * Update mastery scores for multiple words in a transaction.
     */
    @Transaction
    suspend fun updateMasteryScores(updates: List<Pair<Long, Int>>) {
        for ((id, score) in updates) {
            updateMasteryScore(id, score)
        }
    }

    /**
     * Delete a word.
     */
    @Query("DELETE FROM words WHERE id = :id")
    suspend fun deleteWord(id: Long)

    /**
     * Get count of total words.
     */
    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    /**
     * Get count of mastered words.
     */
    @Query("SELECT COUNT(*) FROM words WHERE mastery_score = 100")
    suspend fun getMasteredWordCount(): Int

    /**
     * Get sum of all mastery scores for total points calculation.
     */
    @Query("SELECT SUM(mastery_score) FROM words")
    suspend fun getTotalMasteryScore(): Int

    /**
     * Get words sorted by success rate (for weighted quiz generation).
     * Success rate = correct_answers / (correct_answers + wrong_answers).
     * Returns words with lowest success rate first to prioritize weak words.
     */
    @Query("""
        SELECT * FROM words
        WHERE correct_answers + wrong_answers > 0
        ORDER BY CAST(correct_answers AS REAL) / (correct_answers + wrong_answers) ASC
        LIMIT 10
    """)
    fun getWordsByLowestSuccessRate(): Flow<List<WordEntity>>

    /**
     * Get words that haven't been tested for a long time.
     */
    @Query("SELECT * FROM words ORDER BY last_tested ASC NULLS LAST LIMIT 10")
    fun getWordsNotRecentlyTested(): Flow<List<WordEntity>>

    /**
     * Update notes for a word.
     */
    @Query("UPDATE words SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)
}
