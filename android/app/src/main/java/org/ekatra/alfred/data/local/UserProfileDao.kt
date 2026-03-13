package org.ekatra.alfred.data.local

import androidx.room.*
import org.ekatra.alfred.data.model.UserProfile

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE uid = :uid")
    suspend fun getProfile(uid: String): UserProfile?

    @Upsert
    suspend fun upsert(profile: UserProfile)

    @Query("DELETE FROM user_profiles WHERE uid = :uid")
    suspend fun deleteProfile(uid: String)

    @Query("SELECT * FROM user_profiles LIMIT 1")
    suspend fun getAnyProfile(): UserProfile?
}
