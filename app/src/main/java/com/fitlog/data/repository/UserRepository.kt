package com.fitlog.data.repository

import com.fitlog.data.local.dao.UserDao
import com.fitlog.data.local.entity.UserProfile
import com.fitlog.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Manages user profiles and which one is active. Switching the active user simply
 * changes the stored current-user id; all data reads elsewhere key off that id, so
 * profiles stay fully isolated.
 */
class UserRepository(
    private val userDao: UserDao,
    private val preferences: UserPreferencesRepository,
) {
    val users: Flow<List<UserProfile>> = userDao.observeAll()
    val currentUserId: Flow<Long?> = preferences.currentUserId

    suspend fun userCount(): Int = userDao.count()

    suspend fun getUser(id: Long): UserProfile? = userDao.getById(id)

    suspend fun createUser(name: String, avatarKey: String? = null): Long {
        val id = userDao.insert(UserProfile(name = name.trim(), avatarKey = avatarKey))
        // First user created becomes the active one.
        if (userDao.count() == 1) preferences.setCurrentUser(id)
        return id
    }

    suspend fun selectUser(id: Long) = preferences.setCurrentUser(id)

    suspend fun rename(user: UserProfile, newName: String) =
        userDao.update(user.copy(name = newName.trim()))

    suspend fun deleteUser(user: UserProfile) {
        val wasActive = preferences.currentUserId.first() == user.id
        userDao.delete(user) // cascades to that user's workouts and weight entries
        if (wasActive) preferences.clearCurrentUser()
    }
}
