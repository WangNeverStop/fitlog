package com.fitlog.ui.screens.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlog.data.repository.FinishedWorkoutExercise
import com.fitlog.data.repository.FinishedWorkoutSet
import com.fitlog.data.repository.TrainingRepository
import com.fitlog.data.repository.UserRepository
import com.fitlog.ui.screens.training.action.ExerciseOption
import com.fitlog.ui.screens.training.action.RecordingType
import com.fitlog.ui.screens.training.action.SlotItem
import com.fitlog.ui.screens.training.summary.ExerciseSummary
import com.fitlog.ui.screens.training.summary.SetDetail
import com.fitlog.ui.screens.training.summary.TrainingSummary
import com.fitlog.ui.screens.training.summary.formatSetLabel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/** A set as finished in the session UI; fields used depend on [FinishedExercise.recordingType]. */
data class FinishedSet(
    val weightText: String,
    val reps: Int,
    val durationSec: Int,
    val completed: Boolean,
    val detail: String?,
)

data class FinishedExercise(
    val exerciseId: Long?,
    val name: String,
    val part: TrainingTarget,
    val recordingType: RecordingType,
    val sets: List<FinishedSet>,
)

/** Summary of the last same-target workout offered for reuse (1.1). */
data class ReuseInfo(
    val dateLabel: String,
    val actionNames: List<String>,
    val draft: List<SlotItem>,
)

/**
 * Shared, activity-scoped state for one training session, threading the choices across
 * target → action select → active session → summary, then persisting to Room on finish.
 */
class TrainingSessionViewModel(
    private val userRepository: UserRepository,
    private val trainingRepository: TrainingRepository,
) : ViewModel() {

    var target: TrainingTarget? = null
        private set
    var plan: List<SlotItem> = emptyList()
        private set
    var lastSummary: TrainingSummary? = null
        private set
    var reuseDraft: List<SlotItem> = emptyList()
        private set

    private var startTime: Long? = null

    fun setTarget(target: TrainingTarget) {
        this.target = target
        reuseDraft = emptyList()
    }

    fun setReuse(draft: List<SlotItem>) {
        reuseDraft = draft
    }

    /** Loads the last same-target workout as a reuse offer; null for free/cardio or none. */
    suspend fun loadReuse(): ReuseInfo? {
        val t = target ?: return null
        if (t.isFree) return null
        val userId = userRepository.currentUserId.first() ?: return null
        val draft = trainingRepository.loadReuseDraft(userId, t.name) ?: return null
        val slots = draft.exercises.map { e ->
            SlotItem(
                exercise = ExerciseOption(
                    id = e.exerciseId ?: 0,
                    name = e.name,
                    mainPart = t,
                    intro = "",
                    equipment = "",
                ),
                sets = e.sets,
                reps = e.reps,
                weight = weightLabel(e.weight),
            )
        }
        return ReuseInfo(
            dateLabel = "${draft.date.monthValue}月${draft.date.dayOfMonth}日",
            actionNames = slots.map { it.exercise.name },
            draft = slots,
        )
    }

    fun setSelectedActions(slots: List<SlotItem>) {
        plan = slots
        startTime = System.currentTimeMillis()
    }

    /** Persists the finished session, builds the summary, then invokes [onSaved]. */
    fun finish(finished: List<FinishedExercise>, onSaved: () -> Unit) {
        viewModelScope.launch {
            val userId = userRepository.currentUserId.first()
            val start = startTime ?: System.currentTimeMillis()
            val end = System.currentTimeMillis()
            if (userId != null) {
                trainingRepository.saveCompletedWorkout(
                    userId = userId,
                    targetName = target?.name,
                    isFree = target?.isFree ?: true,
                    startTime = start,
                    endTime = end,
                    exercises = finished.map { fe ->
                        FinishedWorkoutExercise(
                            name = fe.name,
                            exerciseId = fe.exerciseId,
                            recordingType = fe.recordingType.name,
                            sets = fe.sets.map {
                                FinishedWorkoutSet(
                                    weight = it.weightText.toDoubleOrNull() ?: 0.0,
                                    reps = it.reps,
                                    completed = it.completed,
                                    durationSec = it.durationSec,
                                    detail = it.detail,
                                )
                            },
                        )
                    },
                )
            }
            lastSummary = buildSummary(finished, ((end - start) / 60_000L).toInt())
            onSaved()
        }
    }

    fun clear() {
        target = null
        plan = emptyList()
        lastSummary = null
        reuseDraft = emptyList()
        startTime = null
    }

    private fun weightLabel(w: Double): String =
        when {
            w <= 0.0 -> ""
            w % 1.0 == 0.0 -> w.toInt().toString()
            else -> w.toString()
        }

    private fun buildSummary(finished: List<FinishedExercise>, durationMin: Int): TrainingSummary {
        val exs = finished.map { fe ->
            val completed = fe.sets.count { it.completed }
            val rep = fe.sets.firstOrNull { it.completed } ?: fe.sets.firstOrNull()
            val repWeight = rep?.let {
                formatSetLabel(fe.recordingType, it.weightText, it.reps, it.durationSec, it.detail)
            } ?: "-"
            ExerciseSummary(
                name = fe.name,
                part = fe.part,
                completedSets = completed,
                totalSets = fe.sets.size,
                repWeight = repWeight,
                sets = fe.sets.mapIndexed { i, s ->
                    SetDetail(i + 1, formatSetLabel(fe.recordingType, s.weightText, s.reps, s.durationSec, s.detail), s.completed)
                },
            )
        }
        val today = LocalDate.now()
        return TrainingSummary(
            workoutName = (target?.displayName ?: "自由") + "训练",
            dateLabel = "${today.monthValue}月${today.dayOfMonth}日",
            durationMinutes = durationMin,
            completedExercises = exs.count { it.completedSets > 0 },
            completedSets = exs.sumOf { it.completedSets },
            encouragement = "干得漂亮，今天又进步了一点！",
            exercises = exs,
        )
    }
}
