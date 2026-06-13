package com.fitlog.ui.screens.training.reuse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitlog.ui.screens.training.ReuseInfo
import com.fitlog.ui.screens.training.TrainingSessionViewModel
import com.fitlog.ui.screens.training.TrainingTarget
import com.fitlog.ui.screens.training.action.ExerciseOption
import com.fitlog.ui.screens.training.action.SlotItem
import com.fitlog.ui.theme.FitLogTheme

private sealed interface ReuseUiState {
    data object Loading : ReuseUiState
    data object None : ReuseUiState
    data class Has(val info: ReuseInfo) : ReuseUiState
}

/**
 * Reuse-confirm step (PROJECT_LOG 1.1). For a non-free target with history, asks whether
 * to reuse the last same-target workout. If there's no history it auto-skips straight to
 * action selection. Stateful wrapper loads via the shared session VM.
 */
@Composable
fun ReuseConfirmScreen(
    vm: TrainingSessionViewModel,
    onReuse: () -> Unit,
    onCreateNew: () -> Unit,
    onBack: () -> Unit,
) {
    val state by produceState<ReuseUiState>(ReuseUiState.Loading) {
        val info = vm.loadReuse()
        value = if (info == null) ReuseUiState.None else ReuseUiState.Has(info)
    }

    when (val s = state) {
        ReuseUiState.Loading -> LoadingBox()
        ReuseUiState.None -> LaunchedEffect(Unit) { onCreateNew() }
        is ReuseUiState.Has -> ReuseConfirmContent(
            info = s.info,
            targetName = vm.target?.displayName ?: "",
            onReuse = {
                vm.setReuse(s.info.draft)
                onReuse()
            },
            onCreateNew = {
                vm.setReuse(emptyList())
                onCreateNew()
            },
            onBack = onBack,
        )
    }
}

@Composable
private fun LoadingBox() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ReuseConfirmContent(
    info: ReuseInfo,
    targetName: String,
    onReuse: () -> Unit,
    onCreateNew: () -> Unit,
    onBack: () -> Unit,
) {
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

        Spacer(Modifier.height(20.dp))
        Text(
            text = "沿用上次训练？",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "上次 $targetName 训练 · ${info.dateLabel} · ${info.draft.size} 个动作",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                info.draft.forEach { slot ->
                    Column {
                        Text(
                            text = slot.exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = buildString {
                                if (slot.weight.isNotBlank()) append("${slot.weight} · ")
                                append("${slot.sets}组 × ${slot.reps}次")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onReuse,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            Text("沿用上次训练", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onCreateNew,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            Text("重新创建今天的训练")
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Preview(name = "复用确认页", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ReuseConfirmPreview() {
    FitLogTheme {
        val sample = listOf(
            SlotItem(ExerciseOption(1, "杠铃卧推", TrainingTarget.CHEST, "", "杠铃"), 4, 10, "60"),
            SlotItem(ExerciseOption(2, "哑铃飞鸟", TrainingTarget.CHEST, "", "哑铃"), 3, 12, "12"),
            SlotItem(ExerciseOption(3, "俯卧撑", TrainingTarget.CHEST, "", "自重"), 2, 15, ""),
        )
        ReuseConfirmContent(
            info = ReuseInfo(
                dateLabel = "6月5日",
                actionNames = sample.map { it.exercise.name },
                draft = sample,
            ),
            targetName = "胸部",
            onReuse = {},
            onCreateNew = {},
            onBack = {},
        )
    }
}
