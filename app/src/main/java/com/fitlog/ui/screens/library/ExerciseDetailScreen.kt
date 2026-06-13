package com.fitlog.ui.screens.library

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.fitlog.ui.muscle.MuscleMap
import com.fitlog.ui.muscle.toMuscleRegions
import com.fitlog.ui.screens.training.TrainingTarget
import com.fitlog.ui.screens.training.action.ExerciseOption
import com.fitlog.ui.theme.FitLogTheme
import com.fitlog.ui.theme.HoneyDeep

/**
 * Exercise detail (PROJECT_LOG confirmed): muscle map (placeholder), primary/secondary
 * muscles, numbered guidance, cautions, favorite, and add-to-today / add-to-default.
 * System exercises are read-only (no edit/delete). Stateless + previewable.
 */
@Composable
fun ExerciseDetailScreen(
    exercise: ExerciseOption,
    onBack: () -> Unit,
    onAddToToday: () -> Unit = {},
    onAddToDefault: () -> Unit = {},
) {
    var favorite by remember { mutableStateOf(false) }

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

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (favorite) "♥" else "♡",
                style = MaterialTheme.typography.headlineSmall,
                color = HoneyDeep,
                modifier = Modifier.clickable { favorite = !favorite },
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PartTag(exercise.mainPart)
            exercise.secondaryPart?.let { PartTag(it) }
            EquipmentTag(exercise.equipment)
        }

        Spacer(Modifier.height(20.dp))
        MuscleMapCard(exercise)

        Spacer(Modifier.height(20.dp))
        Text("主要肌群：${exercise.mainPart.displayName}", style = MaterialTheme.typography.bodyLarge)
        exercise.secondaryPart?.let {
            Spacer(Modifier.height(4.dp))
            Text("辅助肌群：${it.displayName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle("动作要领")
        val steps = if (exercise.steps.isNotEmpty()) exercise.steps else listOf(exercise.intro.ifBlank { "暂无详细步骤。" })
        steps.forEachIndexed { i, step ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "${i + 1}.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = HoneyDeep,
                    modifier = Modifier.size(width = 24.dp, height = 20.dp),
                )
                Text(step, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("注意事项")
        val cautions = if (exercise.cautions.isNotEmpty()) exercise.cautions else listOf("控制动作幅度与节奏，循序渐进，避免代偿。")
        cautions.forEach { c ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Text("· ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(c, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddToToday,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("加入今日训练", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onAddToDefault,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("加入该部位默认动作")
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun MuscleMapCard(exercise: ExerciseOption) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        val primary = if (exercise.primaryMuscles.isNotEmpty()) exercise.primaryMuscles.toSet()
            else exercise.mainPart.toMuscleRegions()
        val secondary = (if (exercise.secondaryMuscles.isNotEmpty()) exercise.secondaryMuscles.toSet()
            else exercise.secondaryPart?.toMuscleRegions() ?: emptySet()) - primary
        Column(modifier = Modifier.padding(16.dp)) {
            MuscleMap(
                primary = primary,
                secondary = secondary,
                modifier = Modifier.fillMaxWidth().height(220.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "深蓝：主要训练部位 · 浅蓝：辅助部位",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8AA6C0),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(Modifier.height(6.dp))
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

@Composable
private fun EquipmentTag(equipment: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = equipment,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "动作详情页", showBackground = true, widthDp = 360, heightDp = 1000)
@Composable
private fun ExerciseDetailPreview() {
    FitLogTheme {
        ExerciseDetailScreen(
            exercise = ExerciseOption(
                id = 1,
                name = "杠铃卧推",
                mainPart = TrainingTarget.CHEST,
                intro = "平躺推起杠铃，锻炼胸大肌。",
                equipment = "杠铃",
                secondaryPart = TrainingTarget.ARM,
                steps = listOf(
                    "平躺于卧推凳，双脚踩实地面，肩胛骨收紧下沉。",
                    "双手略宽于肩握杠，将杠铃从架上取下置于胸部正上方。",
                    "控制下放杠铃至胸部中线，肘部约 45 度。",
                    "胸部发力将杠铃推回起始位，顶峰不锁死手肘。",
                ),
                cautions = listOf(
                    "下放时不要弹胸借力。",
                    "大重量务必有保护或使用安全杠。",
                ),
            ),
            onBack = {},
        )
    }
}
