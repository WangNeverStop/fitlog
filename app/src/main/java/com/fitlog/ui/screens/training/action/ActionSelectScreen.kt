package com.fitlog.ui.screens.training.action

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.text.KeyboardOptions
import com.fitlog.ui.muscle.MuscleMap
import com.fitlog.ui.muscle.MuscleRegion
import com.fitlog.ui.muscle.toMuscleRegions
import com.fitlog.ui.screens.training.TrainingTarget
import com.fitlog.ui.theme.FitLogTheme
import com.fitlog.ui.theme.HoneyDeep

/**
 * Action-selection page (PROJECT_LOG 1.0.2–1.0.4).
 * - Added actions compact to the top (tapping any empty "+" appends to the list).
 * - Multiple actions can be reordered by long-press drag.
 * - Tapping "+" opens a search sheet → then a params card to fill weight / sets / reps
 *   before the action is added (so the active-training page is display-only).
 * Stateless + previewable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionSelectScreen(
    target: TrainingTarget,
    onConfirm: (List<SlotItem>) -> Unit,
    onBack: () -> Unit,
    initial: List<SlotItem> = emptyList(),
    catalog: List<ExerciseOption> = SampleExercises.all,
) {
    val items = remember { mutableStateListOf<SlotItem>().apply { addAll(initial) } }
    var showSearch by remember { mutableStateOf(false) }
    var pickedExercise by remember { mutableStateOf<ExerciseOption?>(null) }
    var editIndex by remember { mutableStateOf<Int?>(null) }
    val searchSheetState = rememberModalBottomSheetState()
    val paramSheetState = rememberModalBottomSheetState()

    val primaryRegions = items.flatMap { slot ->
        if (slot.exercise.primaryMuscles.isNotEmpty()) slot.exercise.primaryMuscles
        else slot.exercise.mainPart.toMuscleRegions().toList()
    }.toSet()
    val secondaryRegions = items.flatMap { it.exercise.secondaryMuscles }.toSet() - primaryRegions
    val emptySlotCount = (3 - items.size).coerceAtLeast(1)

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
                text = "选择动作",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.size(12.dp))
            PartTag(target)
        }
        if (items.size > 1) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "长按动作可拖动调整顺序",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(16.dp))

        ReorderableSlots(
            items = items,
            onEdit = { index ->
                editIndex = index
                pickedExercise = items[index].exercise
            },
            onDelete = { index -> items.removeAt(index) },
        )

        repeat(emptySlotCount) {
            EmptySlotCard(onClick = { showSearch = true })
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(12.dp))
        MuscleResponsePanel(primaryRegions, secondaryRegions)

        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { onConfirm(items.toList()) },
            enabled = items.isNotEmpty(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            Text("开干！", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(28.dp))
    }

    if (showSearch) {
        ModalBottomSheet(
            onDismissRequest = { showSearch = false },
            sheetState = searchSheetState,
        ) {
            ExerciseSearchSheet(
                catalog = catalog,
                initialPart = target.takeUnless { it.isFree },
                onPick = { ex ->
                    showSearch = false
                    editIndex = null
                    pickedExercise = ex
                },
            )
        }
    }

    val paramExercise = pickedExercise
    if (paramExercise != null) {
        val existing = editIndex?.let { items.getOrNull(it) }
        ModalBottomSheet(
            onDismissRequest = { pickedExercise = null; editIndex = null },
            sheetState = paramSheetState,
        ) {
            ParamsSheet(
                exercise = paramExercise,
                initial = existing,
                onConfirm = { slot ->
                    val idx = editIndex
                    if (idx != null && idx in items.indices) {
                        items[idx] = slot
                    } else {
                        items.add(slot)
                    }
                    pickedExercise = null
                    editIndex = null
                },
            )
        }
    }
}

@Composable
private fun ReorderableSlots(
    items: SnapshotStateList<SlotItem>,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val itemHeightPx = with(LocalDensity.current) { 96.dp.toPx() }
    var draggingIndex by remember { mutableStateOf(-1) }
    var dragOffset by remember { mutableStateOf(0f) }

    Column {
        items.forEachIndexed { index, item ->
            val isDragging = index == draggingIndex
            FilledSlotCard(
                slot = item,
                onEdit = { onEdit(index) },
                onDelete = { onDelete(index) },
                modifier = Modifier
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer { translationY = if (isDragging) dragOffset else 0f },
                dragHandle = Modifier.pointerInput(items.size) {
                    // Immediate drag from the handle (no long-press) for a responsive feel.
                    detectDragGestures(
                        onDragStart = { draggingIndex = index; dragOffset = 0f },
                        onDragEnd = { draggingIndex = -1; dragOffset = 0f },
                        onDragCancel = { draggingIndex = -1; dragOffset = 0f },
                        onDrag = { change, drag ->
                            change.consume()
                            dragOffset += drag.y
                            val cur = draggingIndex
                            if (dragOffset > itemHeightPx / 2 && cur in 0 until items.size - 1) {
                                items.add(cur + 1, items.removeAt(cur))
                                draggingIndex = cur + 1
                                dragOffset -= itemHeightPx
                            } else if (dragOffset < -itemHeightPx / 2 && cur > 0) {
                                items.add(cur - 1, items.removeAt(cur))
                                draggingIndex = cur - 1
                                dragOffset += itemHeightPx
                            }
                        },
                    )
                },
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun EmptySlotCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.5.dp, HoneyDeep.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text("＋", style = MaterialTheme.typography.headlineMedium, color = HoneyDeep)
    }
}

@Composable
private fun FilledSlotCard(
    slot: SlotItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandle: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Drag handle: grab here to reorder (immediate drag, no long-press).
            Box(modifier = dragHandle.padding(end = 8.dp)) {
                Text(
                    text = "≡",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(modifier = Modifier.weight(1f).clickable(onClick = onEdit)) {
                Text(
                    text = slot.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PartTag(slot.exercise.mainPart)
                    Spacer(Modifier.size(10.dp))
                    Text(
                        text = slotSummary(slot),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = "✕",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(onClick = onDelete),
            )
        }
    }
}

private fun slotSummary(slot: SlotItem): String = when (slot.exercise.recordingType) {
    RecordingType.CARDIO -> buildString {
        append("${slot.durationSec / 60} 分钟")
        if (slot.pace.isNotBlank()) append(" · ${slot.pace}")
        if (slot.distance.isNotBlank()) append(" · ${slot.distance}")
    }
    RecordingType.TIMED -> "${slot.sets}组 × ${slot.durationSec}秒"
    RecordingType.BODYWEIGHT -> "自重 · ${slot.sets}组 × ${slot.reps}次"
    RecordingType.STRENGTH -> {
        val w = when {
            slot.weight.isBlank() -> ""
            slot.weight == "自重" -> "自重 · "
            else -> "${slot.weight}kg · "
        }
        "$w${slot.sets}组 × ${slot.reps}次"
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

@Composable
private fun MuscleResponsePanel(
    primaryRegions: Set<MuscleRegion>,
    secondaryRegions: Set<MuscleRegion>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "训练部位反馈",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3A6EA5),
            )
            Spacer(Modifier.height(12.dp))
            MuscleMap(
                primary = primaryRegions,
                secondary = secondaryRegions,
                modifier = Modifier.fillMaxWidth().height(220.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (primaryRegions.isEmpty()) "添加动作后，这里会高亮对应的训练部位。" else "深蓝：本次动作涉及的主要部位",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8AA6C0),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseSearchSheet(
    catalog: List<ExerciseOption>,
    initialPart: TrainingTarget?,
    onPick: (ExerciseOption) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val selectedParts = remember {
        mutableStateListOf<TrainingTarget>().apply { initialPart?.let { add(it) } }
    }
    val selectedEquip = remember { mutableStateListOf<String>() }

    val results = catalog.filter { ex ->
        (selectedParts.isEmpty() || ex.mainPart in selectedParts) &&
            (selectedEquip.isEmpty() || ex.equipment in selectedEquip) &&
            (query.isBlank() || ex.name.contains(query.trim()))
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "添加动作",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("搜索动作名称") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TrainingTarget.gridItems.forEach { part ->
                FilterChip(
                    selected = part in selectedParts,
                    onClick = {
                        if (part in selectedParts) selectedParts.remove(part) else selectedParts.add(part)
                    },
                    label = { Text(part.displayName) },
                )
            }
            ActionFilters.equipments.forEach { equip ->
                FilterChip(
                    selected = equip in selectedEquip,
                    onClick = {
                        if (equip in selectedEquip) selectedEquip.remove(equip) else selectedEquip.add(equip)
                    },
                    label = { Text(equip) },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(results, key = { it.id }) { ex ->
                ExerciseResultRow(ex = ex, onAdd = { onPick(ex) })
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ExerciseResultRow(ex: ExerciseOption, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onAdd)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ex.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${ex.mainPart.displayName} · ${ex.intro}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.size(12.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(HoneyDeep),
            contentAlignment = Alignment.Center,
        ) {
            Text("＋", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
    }
}

@Composable
private fun ParamsSheet(
    exercise: ExerciseOption,
    initial: SlotItem?,
    onConfirm: (SlotItem) -> Unit,
) {
    val type = exercise.recordingType
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        PartTag(exercise.mainPart)
        Spacer(Modifier.height(20.dp))

        when (type) {
            RecordingType.CARDIO -> CardioParams(exercise, initial, onConfirm)
            RecordingType.TIMED -> TimedParams(exercise, initial, onConfirm)
            RecordingType.BODYWEIGHT, RecordingType.STRENGTH -> StrengthParams(exercise, initial, onConfirm, isBodyweightDefault = type == RecordingType.BODYWEIGHT)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ColumnScopeParamsButton(initial: SlotItem?, onClick: () -> Unit) {
    Spacer(Modifier.height(20.dp))
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) {
        Text(if (initial == null) "添加到训练" else "保存修改", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StrengthParams(
    exercise: ExerciseOption,
    initial: SlotItem?,
    onConfirm: (SlotItem) -> Unit,
    isBodyweightDefault: Boolean,
) {
    var bodyweight by remember { mutableStateOf(initial?.weight == "自重" || (initial == null && isBodyweightDefault)) }
    var weight by remember { mutableStateOf(if (initial?.weight == "自重") "" else initial?.weight ?: "") }
    var sets by remember { mutableStateOf((initial?.sets ?: 4).toString()) }
    var reps by remember { mutableStateOf((initial?.reps ?: 10).toString()) }

    OutlinedTextField(
        value = weight,
        onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
        label = { Text("重量") },
        suffix = { Text("kg") },
        enabled = !bodyweight,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.clickable { bodyweight = !bodyweight }, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = bodyweight, onCheckedChange = { bodyweight = it })
        Text("自重（无负重）", style = MaterialTheme.typography.bodyMedium)
    }
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        NumberField(value = sets, onValueChange = { sets = it }, label = "组数", modifier = Modifier.weight(1f))
        NumberField(value = reps, onValueChange = { reps = it }, label = "次数", modifier = Modifier.weight(1f))
    }
    ColumnScopeParamsButton(initial) {
        onConfirm(
            SlotItem(
                exercise = exercise,
                sets = sets.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                reps = reps.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                weight = if (bodyweight) "自重" else weight.trim(),
            ),
        )
    }
}

@Composable
private fun TimedParams(exercise: ExerciseOption, initial: SlotItem?, onConfirm: (SlotItem) -> Unit) {
    var sets by remember { mutableStateOf((initial?.sets ?: 3).toString()) }
    var seconds by remember { mutableStateOf((if ((initial?.durationSec ?: 0) > 0) initial!!.durationSec else 45).toString()) }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        NumberField(value = sets, onValueChange = { sets = it }, label = "组数", modifier = Modifier.weight(1f))
        NumberField(value = seconds, onValueChange = { seconds = it }, label = "每组时长", suffix = "秒", modifier = Modifier.weight(1f))
    }
    ColumnScopeParamsButton(initial) {
        onConfirm(
            SlotItem(
                exercise = exercise,
                sets = sets.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                reps = 0,
                weight = "",
                durationSec = seconds.toIntOrNull()?.coerceAtLeast(1) ?: 30,
            ),
        )
    }
}

@Composable
private fun CardioParams(exercise: ExerciseOption, initial: SlotItem?, onConfirm: (SlotItem) -> Unit) {
    var minutes by remember { mutableStateOf((if ((initial?.durationSec ?: 0) > 0) initial!!.durationSec / 60 else 20).toString()) }
    var pace by remember { mutableStateOf(initial?.pace ?: "") }
    var distance by remember { mutableStateOf(initial?.distance ?: "") }
    NumberField(value = minutes, onValueChange = { minutes = it }, label = "时长", suffix = "分钟", modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = pace,
        onValueChange = { pace = it },
        label = { Text("速度 / 配速（选填，如 8km/h 或 6:00）") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = distance,
        onValueChange = { distance = it },
        label = { Text("距离（选填，如 3km）") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    )
    ColumnScopeParamsButton(initial) {
        onConfirm(
            SlotItem(
                exercise = exercise,
                sets = 1,
                reps = 0,
                weight = "",
                durationSec = (minutes.toIntOrNull()?.coerceAtLeast(1) ?: 20) * 60,
                pace = pace.trim(),
                distance = distance.trim(),
            ),
        )
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suffix: String? = null,
    modifier: Modifier = Modifier,
) {
    val suffixContent: (@Composable () -> Unit)? = suffix?.let { s -> { Text(s) } }
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { c -> c.isDigit() }) },
        label = { Text(label) },
        suffix = suffixContent,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
    )
}

@Preview(name = "动作选择页", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ActionSelectPreview() {
    FitLogTheme {
        ActionSelectScreen(
            target = TrainingTarget.CHEST,
            onConfirm = {},
            onBack = {},
        )
    }
}
