package com.fitlog.ui.screens.training.summary

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitlog.ui.screens.training.TrainingTarget
import com.fitlog.ui.theme.FitLogTheme
import com.fitlog.ui.theme.HoneyDeep
import com.fitlog.ui.theme.HoneyYellow

/**
 * Training-completion summary (PROJECT_LOG 1.4): soft check animation + title, a stats
 * card (name/date/duration/completed exercises/completed sets, no total volume in v1),
 * a single-column per-exercise summary that expands to per-set detail, and primary
 * "完成并返回首页" / secondary "返回继续训练". Stateless + previewable.
 */
@Composable
fun SummaryScreen(
    summary: TrainingSummary,
    onFinishToHome: () -> Unit,
    onBackToTraining: () -> Unit,
) {
    SummaryContent(summary = summary, onFinishToHome = onFinishToHome, onBackToTraining = onBackToTraining)
}

@Composable
fun SummaryContent(
    summary: TrainingSummary,
    onFinishToHome: () -> Unit,
    onBackToTraining: () -> Unit,
) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val checkScale by animateFloatAsState(targetValue = if (shown) 1f else 0f, label = "check")

    val expanded = remember { mutableStateOf(List(summary.exercises.size) { false }) }
    val allExpanded = expanded.value.all { it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(40.dp))
        Box(
            modifier = Modifier
                .size(88.dp)
                .scale(checkScale)
                .clip(CircleShape)
                .background(HoneyYellow),
            contentAlignment = Alignment.Center,
        ) {
            Text("✓", style = MaterialTheme.typography.displayMedium, color = Color.White)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "训练完成",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = summary.encouragement,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))
        StatsCard(summary)

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "本次训练",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (allExpanded) "收起全部" else "展开全部训练数据",
                style = MaterialTheme.typography.labelLarge,
                color = HoneyDeep,
                modifier = Modifier.clickable {
                    expanded.value = List(summary.exercises.size) { !allExpanded }
                },
            )
        }

        Spacer(Modifier.height(12.dp))
        summary.exercises.forEachIndexed { index, ex ->
            ExerciseSummaryCard(
                exercise = ex,
                expanded = expanded.value.getOrElse(index) { false },
                onToggle = {
                    expanded.value = expanded.value.toMutableList().also { it[index] = !it[index] }
                },
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onFinishToHome,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            Text("完成并返回首页", fontWeight = FontWeight.SemiBold)
        }
        TextButton(onClick = onBackToTraining, modifier = Modifier.fillMaxWidth()) {
            Text("返回继续训练", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatsCard(summary: TrainingSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = summary.workoutName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = summary.dateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem("时长", "${summary.durationMinutes} 分", Modifier.weight(1f))
                StatItem("动作", "${summary.completedExercises} 个", Modifier.weight(1f))
                StatItem("完成组数", "${summary.completedSets} 组", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = HoneyDeep,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExerciseSummaryCard(
    exercise: ExerciseSummary,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "完成 ${exercise.completedSets} 组 · ${exercise.repWeight}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = if (expanded) "收起" else "展开",
                    style = MaterialTheme.typography.labelMedium,
                    color = HoneyDeep,
                )
            }

            if (expanded) {
                Spacer(Modifier.height(10.dp))
                exercise.sets.forEach { set ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "第${set.setNumber}组",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = set.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = if (set.completed) "已完成" else "未完成",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (set.completed) HoneyDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
        Text(
            text = part.displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = part.color,
        )
    }
}

@Preview(name = "训练完成总结页", showBackground = true, widthDp = 360, heightDp = 1000)
@Composable
private fun SummaryPreview() {
    FitLogTheme {
        SummaryContent(
            summary = remember { SampleSummary.build() },
            onFinishToHome = {},
            onBackToTraining = {},
        )
    }
}
