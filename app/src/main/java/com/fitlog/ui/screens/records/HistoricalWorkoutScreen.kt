package com.fitlog.ui.screens.records

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitlog.ui.FitLogViewModelFactory
import com.fitlog.ui.screens.training.TrainingTarget
import com.fitlog.ui.screens.training.action.ExerciseOption
import com.fitlog.ui.screens.training.action.RecordingType
import com.fitlog.ui.screens.training.action.SampleExercises
import com.fitlog.ui.theme.HoneyDeep
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class HistoricalExerciseDraft(
    val exercise: ExerciseOption,
    val sets: String,
    val reps: String,
    val weight: String,
    val duration: String,
    val detail: String = "",
)

@Composable
fun HistoricalWorkoutScreen(
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: RecordsViewModel = viewModel(factory = FitLogViewModelFactory.Factory),
) {
    HistoricalWorkoutContent(
        onBack = onBack,
        onSave = { input -> viewModel.addHistoricalWorkout(input, onSaved) },
    )
}

@Composable
private fun HistoricalWorkoutContent(
    onBack: () -> Unit,
    onSave: (HistoricalWorkoutInput) -> Unit,
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    var date by remember { mutableStateOf(today) }
    var startTime by remember { mutableStateOf(LocalTime.of(18, 0)) }
    var endTime by remember { mutableStateOf(LocalTime.of(19, 0)) }
    var target by remember { mutableStateOf(TrainingTarget.CHEST) }
    var showExercisePicker by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val exercises = remember { mutableStateListOf<HistoricalExerciseDraft>() }

    val datePicker = remember(context, date) {
        DatePickerDialog(
            context,
            { _, year, month, day -> date = LocalDate.of(year, month + 1, day) },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth,
        ).apply {
            datePicker.maxDate = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
    }
    val startPicker = remember(context, startTime) {
        TimePickerDialog(
            context,
            { _, hour, minute -> startTime = LocalTime.of(hour, minute) },
            startTime.hour,
            startTime.minute,
            true,
        )
    }
    val endPicker = remember(context, endTime) {
        TimePickerDialog(
            context,
            { _, hour, minute -> endTime = LocalTime.of(hour, minute) },
            endTime.hour,
            endTime.minute,
            true,
        )
    }

    val startMillis = date.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val endMillis = date.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val durationMinutes = ((endMillis - startMillis) / 60_000L).toInt()
    val timeValid = endMillis > startMillis && (date != today || endMillis <= System.currentTimeMillis())
    val exercisesValid = exercises.isNotEmpty() && exercises.all(::isValid)
    val canSave = timeValid && exercisesValid && !date.isAfter(today) && !saving

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
        Text(
            text = "补录历史训练",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "把以前的训练按总结页的形式保存，之后可在记录、日历和首页中查看。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(18.dp))
        SectionCard(title = "训练时间") {
            ValueRow("日期", formatDate(date), onClick = { datePicker.show() })
            ValueRow("开始时间", formatTime(startTime), onClick = { startPicker.show() })
            ValueRow("结束时间", formatTime(endTime), onClick = { endPicker.show() })
            Text(
                text = if (timeValid) {
                    "自动计算：共 $durationMinutes 分钟"
                } else {
                    if (date == today && endMillis > System.currentTimeMillis()) {
                        "结束时间不能晚于现在"
                    } else {
                        "结束时间必须晚于开始时间"
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (timeValid) HoneyDeep else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Spacer(Modifier.height(14.dp))
        SectionCard(title = "训练部位") {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                (TrainingTarget.gridItems + TrainingTarget.FREE).forEach { item ->
                    FilterChip(
                        selected = target == item,
                        onClick = { target = item },
                        label = { Text(item.displayName) },
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "训练动作",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "＋ 添加动作",
                style = MaterialTheme.typography.labelLarge,
                color = HoneyDeep,
                modifier = Modifier.clickable { showExercisePicker = true }.padding(vertical = 8.dp),
            )
        }
        if (exercises.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Text(
                    text = "至少添加一个动作，再填写组数、次数或训练时长。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(18.dp),
                )
            }
        } else {
            exercises.forEachIndexed { index, draft ->
                HistoricalExerciseCard(
                    draft = draft,
                    onChange = { exercises[index] = it },
                    onRemove = { exercises.removeAt(index) },
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                saving = true
                onSave(
                    HistoricalWorkoutInput(
                        date = date,
                        target = target,
                        startTime = startMillis,
                        endTime = endMillis,
                        exercises = exercises.map(::toInput),
                    ),
                )
            },
            enabled = canSave,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(if (saving) "正在保存..." else "保存历史训练", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(28.dp))
    }

    if (showExercisePicker) {
        ExercisePickerDialog(
            target = target,
            selectedIds = exercises.map { it.exercise.id }.toSet(),
            onPick = { exercise ->
                exercises += defaultDraft(exercise)
                showExercisePicker = false
            },
            onDismiss = { showExercisePicker = false },
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ValueRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text("$value  ›", style = MaterialTheme.typography.bodyMedium, color = HoneyDeep)
    }
}

@Composable
private fun HistoricalExerciseCard(
    draft: HistoricalExerciseDraft,
    onChange: (HistoricalExerciseDraft) -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(draft.exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${draft.exercise.mainPart.displayName} · ${recordingLabel(draft.exercise.recordingType)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("删除", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error, modifier = Modifier.clickable(onClick = onRemove))
            }
            Spacer(Modifier.height(12.dp))

            when (draft.exercise.recordingType) {
                RecordingType.STRENGTH -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NumberField(draft.sets, { onChange(draft.copy(sets = it)) }, "组数", Modifier.weight(1f))
                        NumberField(draft.reps, { onChange(draft.copy(reps = it)) }, "次数", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    DecimalField(draft.weight, { onChange(draft.copy(weight = it)) }, "重量", "kg")
                }
                RecordingType.BODYWEIGHT -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NumberField(draft.sets, { onChange(draft.copy(sets = it)) }, "组数", Modifier.weight(1f))
                        NumberField(draft.reps, { onChange(draft.copy(reps = it)) }, "次数", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("按自重动作保存，不记录重量", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                RecordingType.TIMED -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NumberField(draft.sets, { onChange(draft.copy(sets = it)) }, "组数", Modifier.weight(1f))
                        NumberField(draft.duration, { onChange(draft.copy(duration = it)) }, "每组时长", Modifier.weight(1f), "秒")
                    }
                }
                RecordingType.CARDIO -> {
                    NumberField(draft.duration, { onChange(draft.copy(duration = it)) }, "时长", Modifier.fillMaxWidth(), "分钟")
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = draft.detail,
                        onValueChange = { onChange(draft.copy(detail = it)) },
                        label = { Text("速度 / 距离（选填）") },
                        placeholder = { Text("如 8km/h · 3km") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
    suffix: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter(Char::isDigit)) },
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier,
    )
}

@Composable
private fun DecimalField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    suffix: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() || c == '.' }) },
        label = { Text(label) },
        suffix = { Text(suffix) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ExercisePickerDialog(
    target: TrainingTarget,
    selectedIds: Set<Long>,
    onPick: (ExerciseOption) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = SampleExercises.all.filter { exercise ->
        exercise.id !in selectedIds &&
            if (query.isBlank()) {
                target == TrainingTarget.FREE || exercise.mainPart == target
            } else {
                exercise.name.contains(query, ignoreCase = true) ||
                    exercise.mainPart.displayName.contains(query, ignoreCase = true)
            }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("选择训练动作") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("搜索动作或部位") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
                    items(filtered, key = { it.id }) { exercise ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onPick(exercise) }.padding(vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exercise.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text(exercise.mainPart.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("＋", style = MaterialTheme.typography.titleLarge, color = HoneyDeep)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
    )
}

private fun defaultDraft(exercise: ExerciseOption): HistoricalExerciseDraft = when (exercise.recordingType) {
    RecordingType.STRENGTH -> HistoricalExerciseDraft(exercise, "4", "10", "", "")
    RecordingType.BODYWEIGHT -> HistoricalExerciseDraft(exercise, "3", "12", "", "")
    RecordingType.TIMED -> HistoricalExerciseDraft(exercise, "3", "0", "", "45")
    RecordingType.CARDIO -> HistoricalExerciseDraft(exercise, "1", "0", "", "20")
}

private fun isValid(draft: HistoricalExerciseDraft): Boolean = when (draft.exercise.recordingType) {
    RecordingType.STRENGTH -> positiveInt(draft.sets) && positiveInt(draft.reps) && (draft.weight.isBlank() || draft.weight.toDoubleOrNull() != null)
    RecordingType.BODYWEIGHT -> positiveInt(draft.sets) && positiveInt(draft.reps)
    RecordingType.TIMED -> positiveInt(draft.sets) && positiveInt(draft.duration)
    RecordingType.CARDIO -> positiveInt(draft.duration)
}

private fun toInput(draft: HistoricalExerciseDraft): HistoricalExerciseInput {
    val type = draft.exercise.recordingType
    return HistoricalExerciseInput(
        exerciseId = draft.exercise.id,
        name = draft.exercise.name,
        recordingType = type.name,
        sets = if (type == RecordingType.CARDIO) 1 else draft.sets.toIntOrNull()?.coerceAtLeast(1) ?: 1,
        reps = draft.reps.toIntOrNull() ?: 0,
        weight = draft.weight.toDoubleOrNull() ?: 0.0,
        durationSec = when (type) {
            RecordingType.CARDIO -> (draft.duration.toIntOrNull() ?: 0) * 60
            RecordingType.TIMED -> draft.duration.toIntOrNull() ?: 0
            else -> 0
        },
        detail = draft.detail.trim().ifBlank { null },
    )
}

private fun positiveInt(value: String): Boolean = (value.toIntOrNull() ?: 0) > 0

private fun recordingLabel(type: RecordingType): String = when (type) {
    RecordingType.STRENGTH -> "重量与次数"
    RecordingType.BODYWEIGHT -> "自重与次数"
    RecordingType.TIMED -> "计时动作"
    RecordingType.CARDIO -> "有氧时长"
}

private fun formatDate(date: LocalDate): String =
    "${date.year}年${date.monthValue}月${date.dayOfMonth}日"

private fun formatTime(time: LocalTime): String =
    time.format(DateTimeFormatter.ofPattern("HH:mm"))
