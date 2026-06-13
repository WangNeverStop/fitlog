package com.fitlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * A body-weight entry for one user on one date. [userId] keeps each profile's weight
 * history separate; the (userId, date) pair is unique so a day has a single value.
 */
@Entity(
    tableName = "body_weight",
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("userId"),
        Index(value = ["userId", "date"], unique = true),
    ],
)
data class BodyWeight(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val date: LocalDate,
    val weightKg: Double,
)
