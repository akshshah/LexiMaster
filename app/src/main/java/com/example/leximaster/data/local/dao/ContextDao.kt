package com.example.leximaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.leximaster.data.local.entity.ContextEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ContextEntity operations.
 */
@Dao
interface ContextDao {

    /**
     * Get all contexts for a specific word.
     */
    @Query("SELECT * FROM contexts WHERE word_id = :wordId ORDER BY cycle_order ASC")
    fun getContextsByWordId(wordId: Long): Flow<List<ContextEntity>>

    /**
     * Get a context by ID.
     */
    @Query("SELECT * FROM contexts WHERE id = :id")
    suspend fun getContextById(id: Long): ContextEntity?

    /**
     * Get the active context for a word based on its mastery score.
     * Context Cycling Rule:
     * - Score 0–33: Context with cycle_order == 1
     * - Score 34–66: Context with cycle_order == 2
     * - Score 67–100: Context with cycle_order == 3
     */
    @Query("""
        SELECT * FROM contexts
        WHERE word_id = :wordId
        AND cycle_order = CASE
            WHEN :masteryScore <= 33 THEN 1
            WHEN :masteryScore <= 66 THEN 2
            ELSE 3
        END
        LIMIT 1
    """)
    suspend fun getActiveContext(wordId: Long, masteryScore: Int): ContextEntity?

    /**
     * Get contexts by cycle order.
     */
    @Query("SELECT * FROM contexts WHERE word_id = :wordId AND cycle_order = :cycleOrder")
    suspend fun getContextByCycleOrder(wordId: Long, cycleOrder: Int): ContextEntity?

    /**
     * Insert a context. Returns the inserted row ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContext(context: ContextEntity): Long

    /**
     * Insert multiple contexts. Returns list of inserted row IDs.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContexts(contexts: List<ContextEntity>): List<Long>

    /**
     * Update a context.
     */
    @Query("""
        UPDATE contexts
        SET meaning = :meaning, example_usage = :exampleUsage, cycle_order = :cycleOrder
        WHERE id = :id
    """)
    suspend fun updateContext(
        id: Long,
        meaning: String,
        exampleUsage: String,
        cycleOrder: Int,
    )

    /**
     * Delete a context.
     */
    @Query("DELETE FROM contexts WHERE id = :id")
    suspend fun deleteContext(id: Long)

    /**
     * Delete all contexts for a word.
     */
    @Query("DELETE FROM contexts WHERE word_id = :wordId")
    suspend fun deleteContextsByWordId(wordId: Long)
}
