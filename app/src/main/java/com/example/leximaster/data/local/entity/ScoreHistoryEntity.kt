package com.example.leximaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.leximaster.data.local.converter.ScoreChangeReason

/**
 * Immutable ledger of mastery score changes for trend analytics.
 *
 * @property id Primary Key
 * @property wordId Foreign Key to WordEntity.id
 * @property previousScore The score before the change
 * @property newScore The score after the change
 * @property delta The net change (+ or -)
 * @property reason Enum: QUIZ_CORRECT, QUIZ_WRONG, MANUAL_EDIT, SYSTEM_DECAY
 * @property timestamp Epoch ms
 */
@Entity(
    tableName = "score_history",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["word_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["word_id"]),
        Index(value = ["word_id", "change_timestamp"]) // Composite index for trend queries
    ]
)
data class ScoreHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "word_id")
    val wordId: Long,

    @ColumnInfo(name = "previous_score")
    val previousScore: Int,

    @ColumnInfo(name = "new_score")
    val newScore: Int,

    @ColumnInfo(name = "score_delta")
    val delta: Int,

    @ColumnInfo(name = "score_change_reason")
    val reason: ScoreChangeReason,

    @ColumnInfo(name = "change_timestamp")
    val timestamp: Long,
)
