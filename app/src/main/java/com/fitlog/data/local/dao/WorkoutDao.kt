package com.fitlog.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fitlog.data.local.entity.Workout
import com.fitlog.data.local.entity.WorkoutExercise
import com.fitlog.data.local.entity.WorkoutSet
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WorkoutDao {

    // --- writes ---
    @Insert
    suspend fun insertWorkout(workout: Workout): Long

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Insert
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExercise): Long

    @Insert
    suspend fun insertSet(set: WorkoutSet): Long

    @Update
    suspend fun updateSet(set: WorkoutSet)

    @Delete
    suspend fun deleteSet(set: WorkoutSet)

    // --- reads (all scoped to a single userId for isolation) ---
    @Query("SELECT * FROM workout WHERE userId = :userId AND date = :date ORDER BY startTime ASC")
    fun observeByDate(userId: Long, date: LocalDate): Flow<List<Workout>>

    @Query(
        "SELECT * FROM workout WHERE userId = :userId AND date BETWEEN :start AND :end ORDER BY date ASC"
    )
    fun observeBetween(userId: Long, start: LocalDate, end: LocalDate): Flow<List<Workout>>

    /** All completed workouts for the user, newest first (records list). */
    @Query("SELECT * FROM workout WHERE userId = :userId AND completed = 1 ORDER BY date DESC, id DESC")
    fun observeCompleted(userId: Long): Flow<List<Workout>>

    @Query("SELECT * FROM workout WHERE id = :id")
    suspend fun getWorkout(id: Long): Workout?

    /**
     * Template-reuse source: the most recent COMPLETED workout for the same user and
     * the same muscle group. Constrained to userId so it can never pull another
     * profile's session.
     */
    @Query(
        """
        SELECT * FROM workout
        WHERE userId = :userId AND muscleGroupId = :muscleGroupId AND completed = 1
        ORDER BY date DESC, id DESC
        LIMIT 1
        """
    )
    suspend fun findLastCompleted(userId: Long, muscleGroupId: Long): Workout?

    /** Most recent completed workout for the user, regardless of target. */
    @Query(
        """
        SELECT * FROM workout
        WHERE userId = :userId AND completed = 1
        ORDER BY date DESC, id DESC
        LIMIT 1
        """
    )
    suspend fun findLastCompletedAny(userId: Long): Workout?

    /** Most recent completed workout for the same user + training target (for reuse). */
    @Query(
        """
        SELECT * FROM workout
        WHERE userId = :userId AND targetName = :targetName AND completed = 1
        ORDER BY date DESC, id DESC
        LIMIT 1
        """
    )
    suspend fun findLastCompletedByTarget(userId: Long, targetName: String): Workout?

    @Query("SELECT COUNT(*) FROM workout_exercise WHERE workoutId = :workoutId")
    suspend fun exerciseCount(workoutId: Long): Int

    @Query(
        """
        SELECT COUNT(*) FROM workout_set ws
        INNER JOIN workout_exercise we ON ws.workoutExerciseId = we.id
        WHERE we.workoutId = :workoutId AND ws.completed = 1
        """
    )
    suspend fun completedSetCount(workoutId: Long): Int

    @Query("SELECT * FROM workout_exercise WHERE workoutId = :workoutId ORDER BY orderIndex ASC")
    suspend fun getWorkoutExercises(workoutId: Long): List<WorkoutExercise>

    @Query("SELECT * FROM workout_set WHERE workoutExerciseId = :workoutExerciseId ORDER BY setNumber ASC")
    suspend fun getSets(workoutExerciseId: Long): List<WorkoutSet>
}
