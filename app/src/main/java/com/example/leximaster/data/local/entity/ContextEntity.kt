package com.example.leximaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Handles polysemous meanings—one-to-many with words.
 *
 * @property id Primary Key
 * @property wordId Foreign Key to WordEntity.id
 * @property meaning The specific definition for this context
 * @property exampleUsage Sentence usage specific to this meaning
 * @property cycleOrder 1, 2, or 3 - determines which context is displayed/tested based on mastery score
 */
@Entity(
    tableName = "contexts",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["word_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["word_id"])
    ]
)
data class ContextEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "word_id")
    val wordId: Long,

    @ColumnInfo(name = "meaning")
    val meaning: String,

    @ColumnInfo(name = "example_usage")
    val exampleUsage: String,

    @ColumnInfo(name = "cycle_order")
    val cycleOrder: Int,
) {
    companion object {
        const val CYCLE_INTRODUCTION = 1
        const val CYCLE_NUANCED = 2
        const val CYCLE_TECHNICAL = 3

        const val SCORE_CYCLE_1_MAX = 33
        const val SCORE_CYCLE_2_MAX = 66
        const val SCORE_CYCLE_3_MIN = 67
    }
}
