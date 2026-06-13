package com.fitlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single set within a [WorkoutExercise]: weight, reps and whether it was completed.
 */
@Entity(
    tableName = "workout_set",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExercise::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("workoutExerciseId")],
)
data class WorkoutSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutExerciseId: Long,
    val setNumber: Int,
    val weight: Double = 0.0,
    val reps: Int = 0,
    val completed: Boolean = false,
    // For TIMED/CARDIO: duration in seconds; CARDIO extra (pace/distance) in [detail].
    val durationSec: Int = 0,
    val detail: String? = null,
)
