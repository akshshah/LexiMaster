package com.example.leximaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.leximaster.data.local.entity.SynonymEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for SynonymEntity operations.
 */
@Dao
interface SynonymDao {

    /**
     * Get all synonyms for a specific word.
     */
    @Query("SELECT * FROM synonyms WHERE word_id = :wordId")
    fun getSynonymsByWordId(wordId: Long): Flow<List<SynonymEntity>>

    /**
     * Get synonyms by word text (reverse lookup).
     * Useful for word discovery: "show all main words that have 'fast' as a synonym".
     */
    @Query("""
        SELECT s.* FROM synonyms s
        INNER JOIN words w ON s.word_id = w.id
        WHERE s.synonym_text LIKE '%' || :synonymText || '%'
    """)
    suspend fun getWordsWithSynonym(synonymText: String): List<SynonymEntity>

    /**
     * Insert a single synonym. Returns the inserted row ID.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSynonym(synonym: SynonymEntity): Long

    /**
     * Insert multiple synonyms. Returns list of inserted row IDs.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSynonyms(synonyms: List<SynonymEntity>): List<Long>

    /**
     * Delete a synonym.
     */
    @Query("DELETE FROM synonyms WHERE id = :id")
    suspend fun deleteSynonym(id: Int)

    /**
     * Delete all synonyms for a word.
     */
    @Query("DELETE FROM synonyms WHERE word_id = :wordId")
    suspend fun deleteSynonymsByWordId(wordId: Long)

    /**
     * Count synonyms for a word.
     */
    @Query("SELECT COUNT(*) FROM synonyms WHERE word_id = :wordId")
    suspend fun getSynonymCount(wordId: Long): Int
}
