package com.example.leximaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.leximaster.data.local.converter.ScoreChangeReason
import com.example.leximaster.data.local.entity.ScoreHistoryEntity
import com.example.leximaster.data.local.model.AtRiskWordStats
import com.example.leximaster.data.local.model.DailyScoreAverage
import com.example.leximaster.data.local.model.MasteryVelocity
import com.example.leximaster.data.local.model.WordTrendStats
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreHistoryDao {

    @Query("SELECT * FROM score_history ORDER BY change_timestamp DESC")
    fun getAllHistory(): Flow<List<ScoreHistoryEntity>>

    @Query("SELECT * FROM score_history WHERE word_id = :wordId ORDER BY change_timestamp DESC")
    fun getHistoryByWordId(wordId: Long): Flow<List<ScoreHistoryEntity>>

    @Query("""
        SELECT * FROM score_history
        WHERE word_id = :wordId
        AND change_timestamp BETWEEN :startTime AND :endTime
        ORDER BY change_timestamp ASC
    """)
    suspend fun getHistoryByWordIdAndDateRange(
        wordId: Long,
        startTime: Long,
        endTime: Long,
    ): List<ScoreHistoryEntity>

    @Query("""
        SELECT
            DATE(change_timestamp / 1000, 'unixepoch') as day,
            AVG(new_score) as avg_score,
            COUNT(*) as change_count
        FROM score_history
        WHERE change_timestamp BETWEEN :startTime AND :endTime
        GROUP BY day
        ORDER BY day ASC
    """)
    suspend fun getDailyAverages(startTime: Long, endTime: Long): List<DailyScoreAverage>

    @Query("""
        SELECT
            word_id,
            AVG(new_score) as avg_score,
            COUNT(*) as change_count,
            AVG(score_delta) as avg_delta
        FROM score_history
        WHERE change_timestamp > :sinceTimestamp
        GROUP BY word_id
        ORDER BY avg_score DESC
    """)
    suspend fun getWordTrends30Days(sinceTimestamp: Long): List<WordTrendStats>

    @Query("""
        SELECT
            word_id,
            AVG(score_delta) as avg_delta_7day,
            COUNT(*) as change_count
        FROM score_history
        WHERE change_timestamp > :sinceTimestamp
            AND change_timestamp <= :beforeTimestamp
        GROUP BY word_id
        HAVING COUNT(*) > 0
        ORDER BY avg_delta_7day DESC
    """)
    suspend fun getMasteryVelocity7Day(
        sinceTimestamp: Long,
        beforeTimestamp: Long,
    ): List<MasteryVelocity>

    @Query("""
        SELECT
            word_id,
            COUNT(*) as history_count,
            SUM(CASE WHEN score_change_reason = :decayReason THEN 1 ELSE 0 END) as decay_count,
            SUM(CASE WHEN score_delta < 0 THEN 1 ELSE 0 END) as negative_count
        FROM score_history
        WHERE change_timestamp > :sinceTimestamp
        GROUP BY word_id
        HAVING (SUM(CASE WHEN score_change_reason = :decayReason THEN 1 ELSE 0 END) > 0
            OR (SUM(CASE WHEN score_delta < 0 THEN 1 ELSE 0 END) * 1.0 / COUNT(*) > 0.5))
        ORDER BY negative_count DESC
    """)
    suspend fun getAtRiskWords(
        decayReason: ScoreChangeReason,
        sinceTimestamp: Long,
    ): List<AtRiskWordStats>

    @Query("""
        SELECT * FROM score_history
        WHERE score_change_reason = :reason
        ORDER BY change_timestamp DESC
        LIMIT :limit
    """)
    suspend fun getHistoryByReason(reason: ScoreChangeReason, limit: Int): List<ScoreHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ScoreHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(histories: List<ScoreHistoryEntity>): List<Long>

    @Query("DELETE FROM score_history WHERE change_timestamp < :cutoffTimestamp")
    suspend fun deleteHistoryBefore(cutoffTimestamp: Long): Int

    @Query("DELETE FROM score_history WHERE word_id = :wordId")
    suspend fun deleteHistoryByWordId(wordId: Long)

    @Query("SELECT COUNT(*) FROM score_history")
    suspend fun getHistoryCount(): Int

    @Query("""
        SELECT * FROM score_history
        WHERE word_id = :wordId
        ORDER BY change_timestamp DESC
        LIMIT 1
    """)
    suspend fun getLatestHistoryForWord(wordId: Long): ScoreHistoryEntity?
}