package com.fitlog.ui.screens.training.action

import com.fitlog.ui.muscle.MuscleRegion
import com.fitlog.ui.screens.training.TrainingTarget

/**
 * UI-layer model of a selectable exercise. Kept separate from the Room [Exercise]
 * entity so the action-selection UI is previewable with sample data; wiring to the
 * real exercise library (ExerciseDao) is a follow-up (see PROJECT_LOG resume notes).
 */
/** How an exercise is recorded — drives the param sheet, session, summary and detail. */
enum class RecordingType { STRENGTH, BODYWEIGHT, TIMED, CARDIO }

data class ExerciseOption(
    val id: Long,
    val name: String,
    val mainPart: TrainingTarget,
    val intro: String,
    val equipment: String,
    val secondaryPart: TrainingTarget? = null,
    val steps: List<String> = emptyList(),
    val cautions: List<String> = emptyList(),
    val primaryMuscles: List<MuscleRegion> = emptyList(),
    val secondaryMuscles: List<MuscleRegion> = emptyList(),
    val recordingType: RecordingType = RecordingType.STRENGTH,
)

/**
 * A filled action slot with its planned values. Fields used depend on the exercise's
 * [RecordingType]: STRENGTH/BODYWEIGHT use weight+reps+sets; TIMED uses durationSec+sets;
 * CARDIO uses durationSec(+pace/distance), no sets/reps.
 */
data class SlotItem(
    val exercise: ExerciseOption,
    val sets: Int = 4,
    val reps: Int = 10,
    val weight: String = "",
    val durationSec: Int = 0,
    val pace: String = "",
    val distance: String = "",
)

object ActionFilters {
    val equipments = listOf("哑铃", "杠铃", "器械", "自重")
}

// The exercise catalog now lives in the generated SampleExercises.kt (82 actions from
// docs/exercise-library-seed). ExerciseOption.id is Long; generated entries use Int
// literals which Kotlin coerces to Long.
