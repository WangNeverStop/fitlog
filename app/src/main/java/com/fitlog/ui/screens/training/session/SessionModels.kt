package com.fitlog.ui.screens.training.session

import androidx.compose.runtime.toMutableStateList
import com.fitlog.ui.screens.training.TrainingTarget
import com.fitlog.ui.screens.training.action.RecordingType

/**
 * Screen-local state for the active-training page. Fields used depend on [recordingType];
 * the page is display-only and only toggles each set's completion.
 */
class ExerciseRowState(
    val id: Long,
    val name: String,
    val part: TrainingTarget,
    val recordingType: RecordingType,
    val weightText: String,
    val reps: Int,
    val durationSec: Int,
    val pace: String,
    val distance: String,
    plannedSets: Int,
) {
    val setsCompleted = List(plannedSets.coerceAtLeast(1)) { false }.toMutableStateList()

    val completedCount: Int get() = setsCompleted.count { it }
    val totalCount: Int get() = setsCompleted.size

    /** Per-set display line (same for each set; cardio shows its single entry). */
    fun setLine(): String = when (recordingType) {
        RecordingType.CARDIO -> buildString {
            append("${durationSec / 60} 分钟")
            if (pace.isNotBlank()) append(" · $pace")
            if (distance.isNotBlank()) append(" · $distance")
        }
        RecordingType.TIMED -> "$durationSec 秒"
        RecordingType.BODYWEIGHT -> "自重 × $reps 次"
        RecordingType.STRENGTH -> when {
            weightText.isBlank() -> "$reps 次"
            weightText == "自重" -> "自重 × $reps 次"
            else -> "${weightText}kg × $reps 次"
        }
    }
}

object SampleSession {
    fun build(): List<ExerciseRowState> = listOf(
        ExerciseRowState(1, "杠铃卧推", TrainingTarget.CHEST, RecordingType.STRENGTH, "60", 10, 0, "", "", 4).apply {
            setsCompleted[0] = true; setsCompleted[1] = true
        },
        ExerciseRowState(2, "平板支撑", TrainingTarget.ABS, RecordingType.TIMED, "", 0, 45, "", "", 3),
        ExerciseRowState(3, "跑步机跑步", TrainingTarget.CARDIO, RecordingType.CARDIO, "", 0, 1200, "8km/h", "3km", 1),
    )
}
