package com.fitlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * An exercise performed within a [Workout], with its display name denormalized so a
 * saved session does not depend on a matching row in the exercise library (the action
 * catalog is not yet unified with the Room exercise table). [exerciseId] is an optional
 * link for when the library is wired up.
 */
@Entity(
    tableName = "workout_exercise",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("workoutId")],
)
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val exerciseName: String,
    val exerciseId: Long? = null,
    val orderIndex: Int = 0,
    // RecordingType.name (STRENGTH/BODYWEIGHT/TIMED/CARDIO) so detail renders correctly.
    val recordingType: String = "STRENGTH",
)
