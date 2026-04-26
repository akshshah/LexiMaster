package com.example.leximaster.data.local.model

import androidx.room.ColumnInfo
import com.example.leximaster.data.local.converter.QuestionType

data class QuizAttemptWithType(
    val id: Long,
    @ColumnInfo(name = "session_id")
    val sessionId: Long,
    @ColumnInfo(name = "word_id")
    val wordId: Long,
    @ColumnInfo(name = "context_id")
    val contextId: Long,
    @ColumnInfo(name = "question_type")
    val questionType: QuestionType,
    @ColumnInfo(name = "is_correct")
    val isCorrect: Boolean,
    @ColumnInfo(name = "response_time_ms")
    val responseTimeMs: Long,
    @ColumnInfo(name = "session_type")
    val sessionType: String,
)

data class ContextFailureRate(
    val id: Long,
    val meaning: String,
    @ColumnInfo(name = "example_usage")
    val exampleUsage: String,
    @ColumnInfo(name = "total_attempts")
    val totalAttempts: Int,
    val failures: Int,
    @ColumnInfo(name = "failure_rate")
    val failureRate: Double,
)

data class ResponseTimeStats(
    @ColumnInfo(name = "question_type")
    val questionType: QuestionType,
    @ColumnInfo(name = "avg_response_time")
    val avgResponseTime: Double?,
    @ColumnInfo(name = "attempt_count")
    val attemptCount: Int,
)

data class DomainTrendStats(
    val meaning: String,
    @ColumnInfo(name = "attempt_count")
    val attemptCount: Int,
    @ColumnInfo(name = "correct_count")
    val correctCount: Int,
)