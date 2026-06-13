package com.fitlog.data.repository

import com.fitlog.data.local.SeedData
import com.fitlog.data.local.dao.BodyWeightDao
import com.fitlog.data.local.dao.ExerciseDao
import com.fitlog.data.local.dao.MuscleGroupDao
import com.fitlog.data.local.dao.WorkoutDao
import com.fitlog.data.local.entity.BodyWeight
import com.fitlog.data.local.entity.Exercise
import com.fitlog.data.local.entity.MuscleGroup
import com.fitlog.data.local.entity.Workout
import com.fitlog.data.local.entity.WorkoutExercise
import com.fitlog.data.local.entity.WorkoutSet
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Plain finished-exercise data passed from the session UI into [TrainingRepository]. */
data class FinishedWorkoutExercise(
    val name: String,
    val exerciseId: Long?,
    val recordingType: String,
    val sets: List<FinishedWorkoutSet>,
)

data class FinishedWorkoutSet(
    val weight: Double,
    val reps: Int,
    val completed: Boolean,
    val durationSec: Int = 0,
    val detail: String? = null,
)

/** Raw last-workout data for the reuse flow; the VM maps it to UI slot items. */
data class SavedWorkoutDraft(
    val date: java.time.LocalDate,
    val exercises: List<SavedDraftExercise>,
)

data class SavedDraftExercise(
    val exerciseId: Long?,
    val name: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,
)

/** A full saved workout with its exercises and sets (for the records detail page). */
data class WorkoutWithDetail(
    val workout: Workout,
    val exercises: List<WorkoutExerciseDetail>,
)

data class WorkoutExerciseDetail(
    val name: String,
    val recordingType: String,
    val sets: List<WorkoutSet>,
)

/**
 * Training-side data: muscle groups, the exercise library, workouts and body weight.
 * All workout / weight reads are scoped by userId so profiles never see each other's data.
 */
class TrainingRepository(
    private val muscleGroupDao: MuscleGroupDao,
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val bodyWeightDao: BodyWeightDao,
) {
    // --- shared catalog ---
    val muscleGroups: Flow<List<MuscleGroup>> = muscleGroupDao.observeAll()

    fun exercisesFor(userId: Long): Flow<List<Exercise>> = exerciseDao.observeAvailable(userId)

    fun exercisesForMuscleGroup(userId: Long, muscleGroupId: Long): Flow<List<Exercise>> =
        exerciseDao.observeByMuscleGroup(userId, muscleGroupId)

    // --- workouts (per user) ---
    fun workoutsOn(userId: Long, date: LocalDate): Flow<List<Workout>> =
        workoutDao.observeByDate(userId, date)

    fun workoutsBetween(userId: Long, start: LocalDate, end: LocalDate): Flow<List<Workout>> =
        workoutDao.observeBetween(userId, start, end)

    /** Last completed session for the same user + muscle group, used to offer reuse. */
    suspend fun lastCompletedForReuse(userId: Long, muscleGroupId: Long): Workout? =
        workoutDao.findLastCompleted(userId, muscleGroupId)

    /** Most recent completed workout for the user (any target), for the home recent card. */
    suspend fun lastCompletedAny(userId: Long): Workout? = workoutDao.findLastCompletedAny(userId)

    /** All completed workouts for the user, newest first (records list). */
    fun completedWorkouts(userId: Long): Flow<List<Workout>> = workoutDao.observeCompleted(userId)

    /** Loads one workout with its exercises and sets, for the records detail page. */
    suspend fun loadWorkoutDetail(workoutId: Long): WorkoutWithDetail? {
        val workout = workoutDao.getWorkout(workoutId) ?: return null
        val exercises = workoutDao.getWorkoutExercises(workoutId).map { we ->
            WorkoutExerciseDetail(
                name = we.exerciseName,
                recordingType = we.recordingType,
                sets = workoutDao.getSets(we.id),
            )
        }
        return WorkoutWithDetail(workout, exercises)
    }

    suspend fun exerciseCount(workoutId: Long): Int = workoutDao.exerciseCount(workoutId)

    suspend fun completedSetCount(workoutId: Long): Int = workoutDao.completedSetCount(workoutId)

    /**
     * Loads the most recent completed workout for the user + target as a reusable draft,
     * grouping each exercise's sets into a representative sets/reps/weight. Null if none.
     */
    suspend fun loadReuseDraft(userId: Long, targetName: String): SavedWorkoutDraft? {
        val workout = workoutDao.findLastCompletedByTarget(userId, targetName) ?: return null
        val exercises = workoutDao.getWorkoutExercises(workout.id).map { we ->
            val sets = workoutDao.getSets(we.id)
            SavedDraftExercise(
                exerciseId = we.exerciseId,
                name = we.exerciseName,
                sets = sets.size.coerceAtLeast(1),
                reps = sets.firstOrNull()?.reps ?: 10,
                weight = sets.firstOrNull()?.weight ?: 0.0,
            )
        }
        return SavedWorkoutDraft(date = workout.date, exercises = exercises)
    }

    /**
     * Persists a finished session: one [Workout] (completed=true) plus its exercises and
     * sets. Returns the new workout id. Scoped to userId so it stays isolated per profile.
     */
    suspend fun saveCompletedWorkout(
        userId: Long,
        targetName: String?,
        isFree: Boolean,
        startTime: Long,
        endTime: Long,
        exercises: List<FinishedWorkoutExercise>,
        date: LocalDate = LocalDate.now(),
    ): Long {
        val workoutId = workoutDao.insertWorkout(
            Workout(
                userId = userId,
                date = date,
                targetName = targetName,
                isFreeTraining = isFree,
                startTime = startTime,
                endTime = endTime,
                completed = true,
            ),
        )
        exercises.forEachIndexed { index, ex ->
            val weId = workoutDao.insertWorkoutExercise(
                WorkoutExercise(
                    workoutId = workoutId,
                    exerciseName = ex.name,
                    exerciseId = ex.exerciseId,
                    orderIndex = index,
                    recordingType = ex.recordingType,
                ),
            )
            ex.sets.forEachIndexed { setIndex, set ->
                workoutDao.insertSet(
                    WorkoutSet(
                        workoutExerciseId = weId,
                        setNumber = setIndex + 1,
                        weight = set.weight,
                        reps = set.reps,
                        completed = set.completed,
                        durationSec = set.durationSec,
                        detail = set.detail,
                    ),
                )
            }
        }
        return workoutId
    }

    // --- body weight (per user) ---
    fun bodyWeightFor(userId: Long): Flow<List<BodyWeight>> = bodyWeightDao.observeForUser(userId)

    suspend fun upsertBodyWeight(entry: BodyWeight) = bodyWeightDao.upsert(entry)

    /**
     * Inserts built-in muscle groups and starter exercises on first launch only.
     * Idempotent: does nothing if muscle groups already exist.
     */
    suspend fun ensureSeeded() {
        if (muscleGroupDao.count() > 0) return

        muscleGroupDao.insertAll(SeedData.muscleGroups)
        // Re-read to obtain the generated ids, then map exercises onto them by name.
        val idByName = muscleGroupDao.getAll().associateBy({ it.name }, { it.id })

        val exercises = SeedData.exercisesByGroupName.flatMap { (groupName, list) ->
            val groupId = idByName[groupName]
            list.map { (name, instructions) ->
                Exercise(
                    name = name,
                    primaryMuscleGroupId = groupId,
                    instructions = instructions,
                    isCustom = false,
                    ownerUserId = null,
                )
            }
        }
        exerciseDao.insertAll(exercises)
    }
}
