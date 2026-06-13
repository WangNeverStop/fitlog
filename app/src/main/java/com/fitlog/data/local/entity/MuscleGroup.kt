package com.fitlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A training target / muscle group (e.g. Chest, Back, Legs). Shared across all users.
 * [colorHex] is used by the calendar to colour-code which parts were trained on a day.
 */
@Entity(tableName = "muscle_group")
data class MuscleGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
)
