package com.example.leximaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.leximaster.data.local.converter.QuestionType

/**
 * Granular per-question data for domain insights.
 *
 * @property id Primary Key
 * @property sessionId Foreign Key to QuizSessionEntity.id
 * @property wordId Foreign Key to WordEntity.id
 * @property contextId Foreign Key to ContextEntity.id - which meaning was tested
 * @property questionType Enum: RECOGNITION, SYNONYM, RECALL
 * @property isCorrect Boolean - whether the user answered correctly
 * @property responseTimeMs Time taken to answer (fluency metric)
 */
@Entity(
    tableName = "quiz_attempts",
    foreignKeys = [
        ForeignKey(
            entity = QuizSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["word_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContextEntity::class,
            parentColumns = ["id"],
            childColumns = ["context_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["word_id"]),
        Index(value = ["context_id"])
    ]
)
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

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
) {
    companion object {
        const val PRUNE_DAYS = 90
        const val PRUNE_THRESHOLD_MS = PRUNE_DAYS * 24L * 60L * 60L * 1000L
    }
}
