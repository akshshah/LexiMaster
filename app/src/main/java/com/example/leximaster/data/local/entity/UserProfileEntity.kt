package com.example.leximaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Singleton table for global app state.
 * Only one row should ever exist (id = 1).
 *
 * @property id Primary Key, hardcoded to 1
 * @property username User's display name
 * @property totalPoints Sum of all mastery_score values
 * @property currentStreak Consecutive days active
 * @property longestStreak All-time highest streak record
 * @property lastActiveDate Epoch ms timestamp to validate daily streaks
 * @property xpLevel Calculated rank integer
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,

    val username: String = "User",

    @ColumnInfo(name = "total_points")
    val totalPoints: Int = 0,

    @ColumnInfo(name = "current_streak")
    val currentStreak: Int = 1,

    @ColumnInfo(name = "longest_streak")
    val longestStreak: Int = 0,

    @ColumnInfo(name = "last_active_date")
    val lastActiveDate: Long = 0,

    @ColumnInfo(name = "xp_level")
    val xpLevel: Int = 1,
) {
    companion object {
        const val PROFILE_ID = 1
        const val STREAK_INCREMENT_HOURS = 24
        const val STREAK_RESET_HOURS = 48
        const val STREAK_THRESHOLD_MS = STREAK_INCREMENT_HOURS * 60 * 60 * 1000L
        const val STREAK_RESET_THRESHOLD_MS = STREAK_RESET_HOURS * 60 * 60 * 1000L
    }
}
