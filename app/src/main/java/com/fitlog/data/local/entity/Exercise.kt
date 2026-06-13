package com.fitlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * An exercise in the library. Built-in exercises have [ownerUserId] == null and are
 * shared; user-created custom exercises carry the creating user's id.
 *
 * [imageKey] and [externalGuideUrl] are reserved fields: v1 only stores/displays
 * basic instructions, but the schema leaves room for muscle-diagram images and
 * external guide links (v2) without a migration.
 */
@Entity(
    tableName = "exercise",
    foreignKeys = [
        ForeignKey(
            entity = MuscleGroup::class,
            parentColumns = ["id"],
            childColumns = ["primaryMuscleGroupId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = MuscleGroup::class,
            parentColumns = ["id"],
            childColumns = ["secondaryMuscleGroupId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["ownerUserId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("primaryMuscleGroupId"),
        Index("secondaryMuscleGroupId"),
        Index("ownerUserId"),
    ],
)
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val primaryMuscleGroupId: Long? = null,
    val secondaryMuscleGroupId: Long? = null,
    val instructions: String? = null,
    val imageKey: String? = null,
    val externalGuideUrl: String? = null,
    val isCustom: Boolean = false,
    val ownerUserId: Long? = null,
)
