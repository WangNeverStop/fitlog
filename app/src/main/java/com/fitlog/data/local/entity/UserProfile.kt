package com.fitlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A local user profile. Purely for separating different people's data on the same
 * device (Netflix-style profiles) — NOT a security account, so no password field.
 */
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarKey: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
