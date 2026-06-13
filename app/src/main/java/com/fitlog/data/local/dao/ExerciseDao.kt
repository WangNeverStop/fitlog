package com.fitlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fitlog.data.local.entity.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    /** Built-in exercises (ownerUserId IS NULL) plus the current user's custom ones. */
    @Query(
        "SELECT * FROM exercise WHERE ownerUserId IS NULL OR ownerUserId = :userId ORDER BY name ASC"
    )
    fun observeAvailable(userId: Long): Flow<List<Exercise>>

    @Query(
        """
        SELECT * FROM exercise
        WHERE primaryMuscleGroupId = :muscleGroupId
          AND (ownerUserId IS NULL OR ownerUserId = :userId)
        ORDER BY name ASC
        """
    )
    fun observeByMuscleGroup(userId: Long, muscleGroupId: Long): Flow<List<Exercise>>

    @Query("SELECT * FROM exercise WHERE id = :id")
    suspend fun getById(id: Long): Exercise?

    @Query("SELECT COUNT(*) FROM exercise")
    suspend fun count(): Int

    @Insert
    suspend fun insert(exercise: Exercise): Long

    @Insert
    suspend fun insertAll(exercises: List<Exercise>): List<Long>
}
