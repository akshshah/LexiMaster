package com.example.leximaster.data.local.model

import androidx.room.ColumnInfo

data class DailyScoreAverage(
    val day: String,
    @ColumnInfo(name = "avg_score")
    val avgScore: Double,
    @ColumnInfo(name = "change_count")
    val changeCount: Int,
)

data class WordTrendStats(
    @ColumnInfo(name = "word_id")
    val wordId: Long,
    @ColumnInfo(name = "avg_score")
    val avgScore: Double,
    @ColumnInfo(name = "change_count")
    val changeCount: Int,
    @ColumnInfo(name = "avg_delta")
    val avgDelta: Double,
)

data class MasteryVelocity(
    @ColumnInfo(name = "word_id")
    val wordId: Long,
    @ColumnInfo(name = "avg_delta_7day")
    val avgDelta7Day: Double,
    @ColumnInfo(name = "change_count")
    val changeCount: Int,
)

data class AtRiskWordStats(
    @ColumnInfo(name = "word_id")
    val wordId: Long,
    @ColumnInfo(name = "history_count")
    val historyCount: Int,
    @ColumnInfo(name = "decay_count")
    val decayCount: Int,
    @ColumnInfo(name = "negative_count")
    val negativeCount: Int,
)