package com.fitlog.ui.screens.records

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitlog.FitLogApplication
import com.fitlog.data.repository.WorkoutWithDetail
import com.fitlog.ui.screens.training.TrainingTarget
import com.fitlog.ui.screens.training.action.RecordingType
import com.fitlog.ui.screens.training.summary.ExerciseSummary
import com.fitlog.ui.screens.training.summary.SetDetail
import com.fitlog.ui.screens.training.summary.TrainingSummary
import com.fitlog.ui.screens.training.summary.formatSetLabel
import com.fitlog.ui.theme.HoneyDeep

@Composable
fun WorkoutDetailScreen(workoutId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { (context.applicationContext as FitLogApplication).container.trainingRepository }
    val summary by produceState<TrainingSummary?>(initialValue = null, workoutId) {
        value = repo.loadWorkoutDetail(workoutId)?.toSummary()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "‹ 返回",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable(onClick = onBack),
        )

        val s = summary
        if (s == null) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = s.workoutName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${s.dateLabel} · 共 ${s.durationMinutes} 分钟 · ${s.completedExercises} 个动作 · ${s.completedSets} 组",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))
        s.exercises.forEach { ex ->
            DetailExerciseCard(ex)
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DetailExerciseCard(ex: ExerciseSummary) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PartTag(ex.part)
                Spacer(Modifier.size(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(ex.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(2.dp))
                    Text("完成 ${ex.completedSets} 组 · ${ex.repWeight}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(if (expanded) "收起" else "展开", style = MaterialTheme.typography.labelMedium, color = HoneyDeep)
            }
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                ex.sets.forEach { set ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("第${set.setNumber}组", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text(set.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                        Text(if (set.completed) "已完成" else "未完成", style = MaterialTheme.typography.bodySmall, color = if (set.completed) HoneyDeep else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun PartTag(part: TrainingTarget) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(part.color.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(part.displayName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = part.color)
    }
}

private fun WorkoutWithDetail.toSummary(): TrainingSummary {
    val target = workout.targetName?.let { runCatching { TrainingTarget.valueOf(it) }.getOrNull() }
    val part = target ?: TrainingTarget.FREE
    val exs = exercises.map { e ->
        val rt = runCatching { RecordingType.valueOf(e.recordingType) }.getOrDefault(RecordingType.STRENGTH)
        val completed = e.sets.count { it.completed }
        fun labelOf(s: com.fitlog.data.local.entity.WorkoutSet): String {
            val weightText = when {
                rt == RecordingType.BODYWEIGHT -> "自重"
                s.weight <= 0.0 -> ""
                s.weight % 1.0 == 0.0 -> s.weight.toInt().toString()
                else -> s.weight.toString()
            }
            return formatSetLabel(rt, weightText, s.reps, s.durationSec, s.detail)
        }
        val rep = e.sets.firstOrNull { it.completed } ?: e.sets.firstOrNull()
        ExerciseSummary(
            name = e.name,
            part = part,
            completedSets = completed,
            totalSets = e.sets.size,
            repWeight = rep?.let { labelOf(it) } ?: "-",
            sets = e.sets.mapIndexed { i, s -> SetDetail(i + 1, labelOf(s), s.completed) },
        )
    }
    val s = workout.startTime
    val en = workout.endTime
    val minutes = if (s != null && en != null && en > s) ((en - s) / 60_000L).toInt() else 0
    return TrainingSummary(
        workoutName = (target?.displayName ?: "自由") + "训练",
        dateLabel = "${workout.date.monthValue}月${workout.date.dayOfMonth}日",
        durationMinutes = minutes,
        completedExercises = exs.count { it.completedSets > 0 },
        completedSets = exs.sumOf { it.completedSets },
        encouragement = "",
        exercises = exs,
    )
}
