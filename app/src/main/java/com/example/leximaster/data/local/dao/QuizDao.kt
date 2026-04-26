package com.example.leximaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.leximaster.data.local.converter.QuestionType
import com.example.leximaster.data.local.entity.QuizAttemptEntity
import com.example.leximaster.data.local.entity.QuizSessionEntity
import com.example.leximaster.data.local.model.ContextFailureRate
import com.example.leximaster.data.local.model.DomainTrendStats
import com.example.leximaster.data.local.model.QuizAttemptWithType
import com.example.leximaster.data.local.model.ResponseTimeStats
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    @Query("SELECT * FROM quiz_sessions ORDER BY start_timestamp DESC")
    fun getAllSessions(): Flow<List<QuizSessionEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE session_id = :sessionId ORDER BY id ASC")
    suspend fun getAttemptsBySessionId(sessionId: Long): List<QuizAttemptEntity>

    @Query("SELECT * FROM quiz_attempts ORDER BY id DESC LIMIT :limit")
    fun getRecentAttempts(limit: Int): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createSession(session: QuizSessionEntity): Long

    @Query("""
        UPDATE quiz_sessions
        SET end_timestamp = :endTimestamp, total_score_delta = :scoreDelta
        WHERE id = :id
    """)
    suspend fun updateSession(id: Long, endTimestamp: Long, scoreDelta: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempts(attempts: List<QuizAttemptEntity>): List<Long>

    @Query("""
        SELECT qa.*, qs.session_type
        FROM quiz_attempts qa
        JOIN quiz_sessions qs ON qa.session_id = qs.id
        WHERE qa.word_id = :wordId
        ORDER BY qa.id DESC
    """)
    suspend fun getAttemptsByWordId(wordId: Long): List<QuizAttemptWithType>

    @Query("""
        SELECT
            c.id,
            c.meaning,
            c.example_usage,
            COUNT(qa.id) as total_attempts,
            SUM(CASE WHEN qa.is_correct = 0 THEN 1 ELSE 0 END) as failures,
            CAST(SUM(CASE WHEN qa.is_correct = 0 THEN 1 ELSE 0 END) AS REAL) / COUNT(qa.id) as failure_rate
        FROM quiz_attempts qa
        JOIN contexts c ON qa.context_id = c.id
        WHERE qa.session_id IN (
            SELECT id FROM quiz_sessions WHERE start_timestamp > :sinceTimestamp
        )
        GROUP BY c.id
        HAVING COUNT(qa.id) >= :minAttempts
        ORDER BY failure_rate DESC
        LIMIT :limit
    """)
    suspend fun getContextFailureRates(
        sinceTimestamp: Long,
        minAttempts: Int,
        limit: Int,
    ): List<ContextFailureRate>

    @Query("""
        SELECT question_type,
               AVG(response_time_ms) as avg_response_time,
               COUNT(*) as attempt_count
        FROM quiz_attempts
        WHERE session_id IN (
            SELECT id FROM quiz_sessions WHERE start_timestamp > :sinceTimestamp
        )
        GROUP BY question_type
    """)
    suspend fun getAverageResponseTimes(sinceTimestamp: Long): List<ResponseTimeStats>

    @Query("DELETE FROM quiz_attempts WHERE id IN (SELECT id FROM quiz_attempts WHERE id < :cutoffId)")
    suspend fun pruneOldAttempts(cutoffId: Long): Int

    @Query("""
        SELECT c.meaning, COUNT(qa.id) as attempt_count,
               SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) as correct_count
        FROM quiz_attempts qa
        JOIN contexts c ON qa.context_id = c.id
        JOIN words w ON qa.word_id = w.id
        WHERE qa.session_id IN (
            SELECT id FROM quiz_sessions
            WHERE start_timestamp > :sinceTimestamp AND end_timestamp IS NOT NULL
        )
        GROUP BY c.meaning
        ORDER BY attempt_count DESC
    """)
    suspend fun getDomainTrends(sinceTimestamp: Long): List<DomainTrendStats>

    @Query("""
        SELECT * FROM quiz_attempts
        WHERE question_type = :questionType
        AND session_id IN (
            SELECT id FROM quiz_sessions WHERE start_timestamp > :sinceTimestamp
        )
    """)
    suspend fun getAttemptsByQuestionType(
        questionType: QuestionType,
        sinceTimestamp: Long,
    ): List<QuizAttemptEntity>

    @Query("DELETE FROM quiz_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("SELECT COUNT(*) FROM quiz_sessions")
    suspend fun getSessionCount(): Int

    @Query("SELECT COUNT(*) FROM quiz_attempts")
    suspend fun getAttemptCount(): Int
}