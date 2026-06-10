package com.example.leximaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.leximaster.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    fun getUserProfile(id: Int = UserProfileEntity.PROFILE_ID): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    suspend fun getUserProfileSync(id: Int = UserProfileEntity.PROFILE_ID): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createProfile(profile: UserProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET username = :username WHERE id = :id")
    suspend fun updateUsername(id: Int, username: String)

    @Query("UPDATE user_profile SET total_points = :totalPoints WHERE id = :id")
    suspend fun updateTotalPoints(id: Int, totalPoints: Int)

    @Query("""
        UPDATE user_profile
        SET
            current_streak = CASE
                WHEN (:currentTimestamp - last_active_date) >= :streakThreshold 
                     AND (:currentTimestamp - last_active_date) < :resetThreshold
                    THEN current_streak + 1
                WHEN (:currentTimestamp - last_active_date) >= :resetThreshold
                    THEN 1
                ELSE current_streak
            END,
            longest_streak = CASE
                WHEN CASE
                    WHEN (:currentTimestamp - last_active_date) >= :streakThreshold 
                         AND (:currentTimestamp - last_active_date) < :resetThreshold
                        THEN current_streak + 1
                    WHEN (:currentTimestamp - last_active_date) >= :resetThreshold
                        THEN 1
                    ELSE current_streak
                END > :previousLongestStreak
                    THEN CASE
                        WHEN (:currentTimestamp - last_active_date) >= :streakThreshold 
                             AND (:currentTimestamp - last_active_date) < :resetThreshold
                            THEN current_streak + 1
                        WHEN (:currentTimestamp - last_active_date) >= :resetThreshold
                            THEN 1
                        ELSE current_streak
                    END
                ELSE :previousLongestStreak
            END,
            last_active_date = :currentTimestamp
        WHERE id = :id
    """)
    suspend fun incrementStreak(
        id: Int,
        currentTimestamp: Long,
        previousLongestStreak: Int,
        streakThreshold: Long,
        resetThreshold: Long,
    )

    @Query("UPDATE user_profile SET current_streak = 1, last_active_date = :lastActiveDate WHERE id = :id")
    suspend fun resetStreak(id: Int, lastActiveDate: Long)

    @Query("UPDATE user_profile SET last_active_date = :lastActiveDate WHERE id = :id")
    suspend fun updateLastActiveDate(id: Int, lastActiveDate: Long)

    @Query("UPDATE user_profile SET xp_level = :xpLevel WHERE id = :id")
    suspend fun updateXpLevel(id: Int, xpLevel: Int)
}
