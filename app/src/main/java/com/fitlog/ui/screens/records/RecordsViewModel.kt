package com.fitlog.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlog.data.local.entity.Workout
import com.fitlog.data.repository.FinishedWorkoutExercise
import com.fitlog.data.repository.FinishedWorkoutSet
import com.fitlog.data.repository.TrainingRepository
import com.fitlog.data.repository.UserRepository
import com.fitlog.ui.screens.training.TrainingTarget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

data class RecordItem(
    val id: Long,
    val name: String,
    val dateLabel: String,
    val minutes: Int,
)

data class HistoricalExerciseInput(
    val exerciseId: Long?,
    val name: String,
    val recordingType: String,
    val sets: Int,
    val reps: Int = 0,
    val weight: Double = 0.0,
    val durationSec: Int = 0,
    val detail: String? = null,
)

data class HistoricalWorkoutInput(
    val date: LocalDate,
    val target: TrainingTarget,
    val startTime: Long,
    val endTime: Long,
    val exercises: List<HistoricalExerciseInput>,
)

@OptIn(ExperimentalCoroutinesApi::class)
class RecordsViewModel(
    private val userRepository: UserRepository,
    private val trainingRepository: TrainingRepository,
) : ViewModel() {

    val records: StateFlow<List<RecordItem>> =
        userRepository.currentUserId.flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else trainingRepository.completedWorkouts(uid).map { list -> list.map { it.toRecordItem() } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addHistoricalWorkout(input: HistoricalWorkoutInput, onSaved: (Long) -> Unit) {
        if (
            input.date.isAfter(LocalDate.now()) ||
            input.endTime <= input.startTime ||
            input.exercises.isEmpty()
        ) return

        viewModelScope.launch {
            val uid = userRepository.currentUserId.first() ?: return@launch
            val workoutId = trainingRepository.saveCompletedWorkout(
                userId = uid,
                targetName = input.target.name,
                isFree = input.target.isFree,
                startTime = input.startTime,
                endTime = input.endTime,
                date = input.date,
                exercises = input.exercises.map { exercise ->
                    FinishedWorkoutExercise(
                        name = exercise.name,
                        exerciseId = exercise.exerciseId,
                        recordingType = exercise.recordingType,
                        sets = List(exercise.sets.coerceAtLeast(1)) {
                            FinishedWorkoutSet(
                                weight = exercise.weight,
                                reps = exercise.reps,
                                completed = true,
                                durationSec = exercise.durationSec,
                                detail = exercise.detail,
                            )
                        },
                    )
                },
            )
            onSaved(workoutId)
        }
    }
}

private fun Workout.toRecordItem(): RecordItem {
    val target = targetName?.let { runCatching { TrainingTarget.valueOf(it) }.getOrNull() }
    val minutes = if (startTime != null && endTime != null && endTime > startTime) {
        ((endTime - startTime) / 60_000L).toInt()
    } else {
        0
    }
    return RecordItem(
        id = id,
        name = (target?.displayName ?: "自由") + "训练",
        dateLabel = "${date.monthValue}月${date.dayOfMonth}日",
        minutes = minutes,
    )
}
