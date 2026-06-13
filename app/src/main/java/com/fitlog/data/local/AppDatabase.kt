package com.fitlog.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fitlog.data.local.dao.BodyWeightDao
import com.fitlog.data.local.dao.ExerciseDao
import com.fitlog.data.local.dao.MuscleGroupDao
import com.fitlog.data.local.dao.UserDao
import com.fitlog.data.local.dao.WorkoutDao
import com.fitlog.data.local.entity.BodyWeight
import com.fitlog.data.local.entity.Exercise
import com.fitlog.data.local.entity.MuscleGroup
import com.fitlog.data.local.entity.UserProfile
import com.fitlog.data.local.entity.Workout
import com.fitlog.data.local.entity.WorkoutExercise
import com.fitlog.data.local.entity.WorkoutSet

@Database(
    entities = [
        UserProfile::class,
        MuscleGroup::class,
        Exercise::class,
        Workout::class,
        WorkoutExercise::class,
        WorkoutSet::class,
        BodyWeight::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun muscleGroupDao(): MuscleGroupDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun bodyWeightDao(): BodyWeightDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitlog.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
