package com.example.leximaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The primary entity for storing vocabulary.
 *
 * @property id Primary Key, auto-generated
 * @property word The vocabulary word (unique)
 * @property phonetic IPA pronunciation text (optional)
 * @property notes Custom user context/memory joggers (optional)
 * @property masteryScore 0-100 mastery points
 * @property correctAnswers Cumulative successful quiz attempts
 * @property wrongAnswers Cumulative failed quiz attempts
 * @property createdAt Epoch ms timestamp for "Newest/Oldest" UI sorting
 * @property lastTested Epoch ms timestamp of the last quiz attempt
 */
@Entity(
    tableName = "words",
    indices = [
        Index(value = ["word"], unique = true),
        Index(value = ["mastery_score"]),
        Index(value = ["created_at"]),
        Index(value = ["last_tested"])
    ]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "word")
    val word: String,

    @ColumnInfo(name = "phonetic")
    val phonetic: String?,

    @ColumnInfo(name = "notes")
    val notes: String?,

    @ColumnInfo(name = "mastery_score")
    val masteryScore: Int = 0,

    @ColumnInfo(name = "correct_answers")
    val correctAnswers: Int = 0,

    @ColumnInfo(name = "wrong_answers")
    val wrongAnswers: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "last_tested")
    val lastTested: Long? = null,
) {
    companion object {
        const val MASTERY_MAX = 100
        const val MASTERY_NOVICE_MAX = 30
        const val MASTERY_COMPETENT_MAX = 70
        const val MASTERY_EXPERT_MAX = 99

        const val POINTS_CORRECT_NOVICE = 10
        const val POINTS_CORRECT_EXPERT = 5
        const val POINTS_WRONG = -10
        const val POINTS_DECAY = -5

        const val DECAY_THRESHOLD_MS = 30 * 24 * 60 * 60 * 1000L // 30 days
    }
}
