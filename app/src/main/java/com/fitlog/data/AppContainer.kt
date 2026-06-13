package com.fitlog.data

import android.content.Context
import com.fitlog.data.local.AppDatabase
import com.fitlog.data.prefs.UserPreferencesRepository
import com.fitlog.data.repository.TrainingRepository
import com.fitlog.data.repository.UserRepository

/**
 * Manual dependency container. A small personal app doesn't need a DI framework, so
 * we build the database, preferences and repositories once and hand them out.
 */
class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)
    val userPreferences = UserPreferencesRepository(context)

    val userRepository = UserRepository(
        userDao = database.userDao(),
        preferences = userPreferences,
    )

    val trainingRepository = TrainingRepository(
        muscleGroupDao = database.muscleGroupDao(),
        exerciseDao = database.exerciseDao(),
        workoutDao = database.workoutDao(),
        bodyWeightDao = database.bodyWeightDao(),
    )
}
