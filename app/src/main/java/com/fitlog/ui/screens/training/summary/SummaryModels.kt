package com.fitlog.ui.screens.training.summary

import com.fitlog.ui.screens.training.TrainingTarget
import com.fitlog.ui.screens.training.action.RecordingType

/** One set inside an exercise's expanded detail. [label] is already formatted per type. */
data class SetDetail(
    val setNumber: Int,
    val label: String,
    val completed: Boolean,
)

/** Per-exercise summary row (collapsed shows the representative line; expand for sets). */
data class ExerciseSummary(
    val name: String,
    val part: TrainingTarget,
    val completedSets: Int,
    val totalSets: Int,
    val repWeight: String, // representative line, e.g. "60kg × 10次" / "45 秒" / "20 分钟"
    val sets: List<SetDetail>,
)

data class TrainingSummary(
    val workoutName: String,
    val dateLabel: String,
    val durationMinutes: Int,
    val completedExercises: Int,
    val completedSets: Int,
    val encouragement: String,
    val exercises: List<ExerciseSummary>,
)

/** Formats one set's display line by recording type. Shared by summary and record detail. */
fun formatSetLabel(
    type: RecordingType,
    weightText: String,
    reps: Int,
    durationSec: Int,
    detail: String?,
): String = when (type) {
    RecordingType.CARDIO -> buildString {
        append("${durationSec / 60} 分钟")
        if (!detail.isNullOrBlank()) append(" · $detail")
    }
    RecordingType.TIMED -> "$durationSec 秒"
    RecordingType.BODYWEIGHT -> "自重 × $reps 次"
    RecordingType.STRENGTH -> when {
        weightText.isBlank() -> "$reps 次"
        weightText == "自重" -> "自重 × $reps 次"
        else -> "${weightText}kg × $reps 次"
    }
}

object SampleSummary {
    fun build(): TrainingSummary = TrainingSummary(
        workoutName = "胸部训练",
        dateLabel = "6月8日",
        durationMinutes = 52,
        completedExercises = 3,
        completedSets = 9,
        encouragement = "干得漂亮，今天又进步了一点！",
        exercises = listOf(
            ExerciseSummary(
                name = "杠铃卧推",
                part = TrainingTarget.CHEST,
                completedSets = 4,
                totalSets = 4,
                repWeight = "60kg × 10次",
                sets = listOf(
                    SetDetail(1, "60kg × 10次", true),
                    SetDetail(2, "60kg × 10次", true),
                    SetDetail(3, "65kg × 8次", true),
                    SetDetail(4, "65kg × 8次", true),
                ),
            ),
            ExerciseSummary(
                name = "平板支撑",
                part = TrainingTarget.ABS,
                completedSets = 3,
                totalSets = 3,
                repWeight = "45 秒",
                sets = listOf(
                    SetDetail(1, "45 秒", true),
                    SetDetail(2, "45 秒", true),
                    SetDetail(3, "45 秒", true),
                ),
            ),
        ),
    )
}
