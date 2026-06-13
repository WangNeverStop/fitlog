package com.fitlog.ui.screens.training.session

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.time.LocalTime
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitlog.ui.screens.training.FinishedExercise
import com.fitlog.ui.screens.training.FinishedSet
import com.fitlog.ui.screens.training.TrainingTarget
import com.fitlog.ui.screens.training.action.RecordingType
import com.fitlog.ui.screens.training.action.SlotItem
import com.fitlog.ui.theme.FitLogTheme
import com.fitlog.ui.theme.HoneyDeep

/**
 * Active-training record page (PROJECT_LOG 1.3): single-column action cards in an
 * accordion — the current action is expanded with editable set rows, the rest are
 * collapsed summaries; tapping a collapsed card expands it and collapses the others.
 * Copy/delete-set and the rest timer are deferred per spec. Stateless + previewable;
 * receiving the real selected actions and persisting to Room are follow-ups.
 */
@Composable
fun SessionScreen(
    plan: List<SlotItem>,
    onFinish: (List<FinishedExercise>) -> Unit,
    onBack: () -> Unit,
) {
    val exercises = remember(plan) {
        if (plan.isEmpty()) {
            SampleSession.build()
        } else {
            plan.map { slot ->
                ExerciseRowState(
                    id = slot.exercise.id,
                    name = slot.exercise.name,
                    part = slot.exercise.mainPart,
                    recordingType = slot.exercise.recordingType,
                    weightText = slot.weight,
                    reps = slot.reps,
                    durationSec = slot.durationSec,
                    pace = slot.pace,
                    distance = slot.distance,
                    plannedSets = slot.sets,
                )
            }
        }
    }
    SessionContent(exercises = exercises, onFinish = onFinish, onBack = onBack)
}

@Composable
fun SessionContent(
    exercises: List<ExerciseRowState>,
    onFinish: (List<FinishedExercise>) -> Unit,
    onBack: () -> Unit,
) {
    var expandedId by remember { mutableStateOf(exercises.firstOrNull()?.id) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val incompleteCount = exercises.sumOf { it.totalCount - it.completedCount }

    // Warn on system back so an accidental press doesn't drop the in-progress session.
    BackHandler(enabled = true) { showExitDialog = true }

    // Session timer: start time + a live elapsed counter (ticks each second).
    val startMs = remember { System.currentTimeMillis() }
    val startLabel = remember { LocalTime.now().let { "%02d:%02d".format(it.hour, it.minute) } }
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1000)
        }
    }
    val elapsedSec = ((nowMs - startMs) / 1000).coerceAtLeast(0)
    val elapsedLabel = "%d:%02d".format(elapsedSec / 60, elapsedSec % 60)

    val collectFinished: () -> List<FinishedExercise> = {
        exercises.map { ex ->
            val detail = if (ex.recordingType == RecordingType.CARDIO) {
                listOf(ex.pace, ex.distance).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { null }
            } else {
                null
            }
            FinishedExercise(
                exerciseId = ex.id,
                name = ex.name,
                part = ex.part,
                recordingType = ex.recordingType,
                sets = ex.setsCompleted.map { done ->
                    FinishedSet(ex.weightText, ex.reps, ex.durationSec, done, detail)
                },
            )
        }
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
            modifier = Modifier.clickable { showExitDialog = true },
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "正式训练",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "开始 $startLabel · 已用时 $elapsedLabel",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))

        exercises.forEach { ex ->
            ExerciseCard(
                exercise = ex,
                expanded = ex.id == expandedId,
                onToggle = { expandedId = if (expandedId == ex.id) null else ex.id },
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { if (incompleteCount > 0) showFinishDialog = true else onFinish(collectFinished()) },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            Text("结束训练", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(28.dp))
    }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("还有未完成的组") },
            text = { Text("当前还有 $incompleteCount 组未完成，未完成的组不计入统计。确定结束本次训练吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    onFinish(collectFinished())
                }) { Text("忽略并结束") }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) { Text("继续训练") }
            },
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("正在训练中") },
            text = { Text("要离开当前训练吗？当前记录可能不会保存。") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onBack()
                }) { Text("离开") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("继续训练") }
            },
        )
    }
}

@Composable
private fun ExerciseCard(
    exercise: ExerciseRowState,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 3.dp else 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PartTag(exercise.part)
                Spacer(Modifier.size(10.dp))
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "完成 ${exercise.completedCount}/${exercise.totalCount} 组",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                exercise.setsCompleted.forEachIndexed { i, done ->
                    SetDisplayRow(
                        setNumber = i + 1,
                        label = exercise.setLine(),
                        completed = done,
                        onToggle = { exercise.setsCompleted[i] = !done },
                    )
                }
            }
        }
    }
}

/** Display-only set row: shows the planned weight × reps; the only action is the check. */
/** Display-only set row: shows the planned line; the only action is the check. */
@Composable
private fun SetDisplayRow(
    setNumber: Int,
    label: String,
    completed: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "第${setNumber}组",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(52.dp),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        Checkbox(checked = completed, onCheckedChange = { onToggle() })
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
        Text(
            text = part.displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = part.color,
        )
    }
}

@Preview(name = "正式训练记录页", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun SessionPreview() {
    FitLogTheme {
        SessionContent(
            exercises = remember { SampleSession.build() },
            onFinish = {},
            onBack = {},
        )
    }
}
