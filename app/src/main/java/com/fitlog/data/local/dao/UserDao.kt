package com.fitlog.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fitlog.data.local.entity.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profile WHERE id = :id")
    suspend fun getById(id: Long): UserProfile?

    @Query("SELECT COUNT(*) FROM user_profile")
    suspend fun count(): Int

    @Insert
    suspend fun insert(user: UserProfile): Long

    @Update
    suspend fun update(user: UserProfile)

    @Delete
    suspend fun delete(user: UserProfile)
}
