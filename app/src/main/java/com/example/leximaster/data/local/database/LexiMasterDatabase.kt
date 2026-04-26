package com.example.leximaster.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.leximaster.data.local.converter.LexiMasterConverters
import com.example.leximaster.data.local.dao.ContextDao
import com.example.leximaster.data.local.dao.QuizDao
import com.example.leximaster.data.local.dao.ScoreHistoryDao
import com.example.leximaster.data.local.dao.SynonymDao
import com.example.leximaster.data.local.dao.UserDao
import com.example.leximaster.data.local.dao.WordDao
import com.example.leximaster.data.local.entity.ContextEntity
import com.example.leximaster.data.local.entity.QuizAttemptEntity
import com.example.leximaster.data.local.entity.QuizSessionEntity
import com.example.leximaster.data.local.entity.ScoreHistoryEntity
import com.example.leximaster.data.local.entity.SynonymEntity
import com.example.leximaster.data.local.entity.UserProfileEntity
import com.example.leximaster.data.local.entity.WordEntity

/**
 * Room Database for LexiMaster.
 *
 * Entities:
 * - WordEntity: Primary vocabulary storage
 * - ContextEntity: Polysemous meanings (one-to-many with words)
 * - SynonymEntity: Normalized synonyms for reverse-lookup
 * - UserProfileEntity: Singleton user state
 * - QuizSessionEntity: Quiz session header
 * - QuizAttemptEntity: Per-question analytics
 * - ScoreHistoryEntity: Mastery score change ledger
 *
 * TypeConverters:
 * - QuestionType: Enum handling for quiz question types
 * - ScoreChangeReason: Enum handling for score history reasons
 */
@Database(
    entities = [
        WordEntity::class,
        ContextEntity::class,
        SynonymEntity::class,
        UserProfileEntity::class,
        QuizSessionEntity::class,
        QuizAttemptEntity::class,
        ScoreHistoryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    LexiMasterConverters::class
)
abstract class LexiMasterDatabase : RoomDatabase() {

    /**
     * DAO for WordEntity operations.
     */
    abstract fun wordDao(): WordDao

    /**
     * DAO for ContextEntity operations.
     */
    abstract fun contextDao(): ContextDao

    /**
     * DAO for SynonymEntity operations.
     */
    abstract fun synonymDao(): SynonymDao

    /**
     * DAO for Quiz operations (sessions and attempts).
     */
    abstract fun quizDao(): QuizDao

    /**
     * DAO for UserProfile operations.
     */
    abstract fun userDao(): UserDao

    /**
     * DAO for ScoreHistory operations.
     */
    abstract fun scoreHistoryDao(): ScoreHistoryDao

    companion object {
        const val DATABASE_NAME = "leximaster_db"
    }
}
