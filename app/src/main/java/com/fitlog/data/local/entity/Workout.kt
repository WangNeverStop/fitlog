package com.fitlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * One training session for one user on one date.
 *
 * [userId] is the data-isolation key: every read of workouts filters by it, so one
 * profile can never see another profile's sessions.
 *
 * [muscleGroupId] == null together with [isFreeTraining] == true represents the
 * "free / no specific part" training (running, ad-hoc), which is never reused as a
 * template.
 */
@Entity(
    tableName = "workout",
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MuscleGroup::class,
            parentColumns = ["id"],
            childColumns = ["muscleGroupId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("userId"), Index("muscleGroupId")],
)
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val date: LocalDate,
    val muscleGroupId: Long? = null,
    // The selected TrainingTarget.name (e.g. "CHEST"). Drives home dots / recent name /
    // calendar without needing a muscle_group row. null = free/cardio with no target.
    val targetName: String? = null,
    val isFreeTraining: Boolean = false,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val note: String? = null,
    val completed: Boolean = false,
)
