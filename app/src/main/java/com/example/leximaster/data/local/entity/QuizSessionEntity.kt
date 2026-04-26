package com.example.leximaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Quiz session header for analytics.
 *
 * @property id Primary Key
 * @property startTimestamp Epoch ms, quiz started
 * @property endTimestamp Epoch ms, quiz completed
 * @property totalScoreDelta Sum of points gained/lost this session
 * @property sessionType e.g., "Daily Review", "Category Sprint"
 */
@Entity(tableName = "quiz_sessions")
data class QuizSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "start_timestamp")
    val startTimestamp: Long,

    @ColumnInfo(name = "end_timestamp")
    val endTimestamp: Long,

    @ColumnInfo(name = "total_score_delta")
    val totalScoreDelta: Int,

    @ColumnInfo(name = "session_type")
    val sessionType: String,
)
