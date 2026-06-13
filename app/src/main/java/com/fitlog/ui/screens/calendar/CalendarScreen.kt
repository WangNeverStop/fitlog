package com.fitlog.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitlog.ui.FitLogViewModelFactory
import com.fitlog.ui.components.DetailedWeightChart
import com.fitlog.ui.components.WeightChartPoint
import com.fitlog.ui.theme.FitLogTheme
import com.fitlog.ui.theme.HoneyDeep
import java.time.LocalDate

private val weekdayLabels = listOf("一", "二", "三", "四", "五", "六", "日")

@Composable
fun CalendarScreen(
    onOpenWorkout: (Long) -> Unit,
    onOpenWeight: () -> Unit,
    viewModel: CalendarViewModel = viewModel(factory = FitLogViewModelFactory.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val weight by viewModel.weight.collectAsStateWithLifecycle()
    val memos by viewModel.memos.collectAsStateWithLifecycle()
    CalendarContent(
        state = state,
        weight = weight,
        memos = memos,
        onPrev = viewModel::prevMonth,
        onNext = viewModel::nextMonth,
        onSelect = viewModel::selectDate,
        onAddWeight = viewModel::addWeight,
        onAddMemo = viewModel::addMemo,
        onOpenWorkout = onOpenWorkout,
        onOpenWeight = onOpenWeight,
    )
}

@Composable
fun CalendarContent(
    state: CalendarUiState,
    weight: WeightSummary,
    memos: Set<String>,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelect: (LocalDate) -> Unit,
    onAddWeight: (Double) -> Unit,
    onAddMemo: (LocalDate, String) -> Unit,
    onOpenWorkout: (Long) -> Unit,
    onOpenWeight: () -> Unit,
) {
    var showWeightDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("‹", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable(onClick = onPrev).padding(horizontal = 16.dp))
            Text(
                text = state.monthLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable(onClick = onNext).padding(horizontal = 16.dp))
        }

        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdayLabels.forEach { w ->
                Text(
                    text = w,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        state.days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    val mmdd = "%02d-%02d".format(day.date.monthValue, day.date.dayOfMonth)
                    DayCell(
                        day = day,
                        selected = day.date == state.selectedDate,
                        hasMemo = memos.any { it.startsWith("$mmdd|") },
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(day.date) },
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = "本月训练 ${state.monthCount} 次",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))
        SelectedDayPanel(state = state, memos = memos, onAddMemo = onAddMemo, onOpenWorkout = onOpenWorkout)

        Spacer(Modifier.height(16.dp))
        WeightTrendCard(
            weight = weight,
            onRecord = { showWeightDialog = true },
            onOpenDetails = onOpenWeight,
        )
        Spacer(Modifier.height(24.dp))
    }

    if (showWeightDialog) {
        WeightRecordDialog(
            onConfirm = { kg ->
                onAddWeight(kg)
                showWeightDialog = false
            },
            onDismiss = { showWeightDialog = false },
        )
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    selected: Boolean,
    hasMemo: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val numberColor = when {
        !day.inMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
        day.holiday != null -> Color(0xFFD9544D)
        else -> MaterialTheme.colorScheme.onBackground
    }
    // Square cell: holiday name top, number dead-center, training/memo marks at bottom.
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (selected) Modifier.border(1.5.dp, HoneyDeep, RoundedCornerShape(14.dp)) else Modifier,
            )
            .clickable(onClick = onClick),
    ) {
        // Specific holiday name at the top.
        if (day.holiday != null) {
            Text(
                text = day.holiday,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFD9544D),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 3.dp),
            )
        }

        // Number, dead-center; today is circled with a deeper colour.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(30.dp)
                .then(if (day.isToday) Modifier.clip(CircleShape).background(HoneyDeep) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (day.isToday) Color.White else numberColor,
            )
        }

        // Marks at the bottom: trained dot + memo star.
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (day.partColors.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(day.partColors.first()),
                )
            }
            if (hasMemo) {
                Text("★", color = HoneyDeep, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun SelectedDayPanel(
    state: CalendarUiState,
    memos: Set<String>,
    onAddMemo: (java.time.LocalDate, String) -> Unit,
    onOpenWorkout: (Long) -> Unit,
) {
    val date = state.selectedDate ?: return
    var showMemoDialog by remember(date) { mutableStateOf(false) }
    val mmdd = "%02d-%02d".format(date.monthValue, date.dayOfMonth)
    val dayMemos = memos.filter { it.startsWith("$mmdd|") }.map { it.substringAfter("|") }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        val info = Holidays2026.forDate(date)
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "${date.monthValue}月${date.dayOfMonth}日",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            // Order per spec: 1) holiday, 2) personal memo, 3) training.
            Spacer(Modifier.height(12.dp))
            PanelSection(label = "节假日") {
                Text(
                    text = if (info != null) "${info.shortName}（${info.label}）· ${info.description}" else "无",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        info == null -> MaterialTheme.colorScheme.onSurfaceVariant
                        info.isWorkday -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> Color(0xFFD9544D)
                    },
                )
            }

            Spacer(Modifier.height(12.dp))
            PanelSection(label = "备忘录") {
                if (dayMemos.isEmpty()) {
                    Text("暂无", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    dayMemos.forEach { m ->
                        Text("· $m", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "＋ 添加备忘录",
                    style = MaterialTheme.typography.labelLarge,
                    color = HoneyDeep,
                    modifier = Modifier.clickable { showMemoDialog = true },
                )
            }

            Spacer(Modifier.height(12.dp))
            PanelSection(label = "训练") {
                if (state.selectedSummaries.isEmpty()) {
                    Text("无训练记录", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    state.selectedSummaries.forEach { s ->
                        Text(
                            text = "${s.targetName}训练 · ${s.minutes} 分钟  ›",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HoneyDeep,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenWorkout(s.workoutId) }
                                .padding(vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }

    if (showMemoDialog) {
        MemoDialog(
            onConfirm = { text ->
                onAddMemo(date, text)
                showMemoDialog = false
            },
            onDismiss = { showMemoDialog = false },
        )
    }
}

@Composable
private fun MemoDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("添加备忘录") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("内容（如：生日、纪念日）") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun PanelSection(label: String, content: @Composable () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(4.dp))
    content()
}

@Composable
private fun WeightTrendCard(
    weight: WeightSummary,
    onRecord: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "体重趋势",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                Text("查看详情", style = MaterialTheme.typography.labelLarge, color = HoneyDeep, modifier = Modifier.clickable(onClick = onOpenDetails))
                Spacer(Modifier.size(12.dp))
                Text("记录", style = MaterialTheme.typography.labelLarge, color = HoneyDeep, modifier = Modifier.clickable(onClick = onRecord))
            }
            Spacer(Modifier.height(10.dp))
            if (weight.current == null) {
                Text(
                    text = "还没有体重记录，点「记录」添加今天的体重。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${trimWeight(weight.current)} kg",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    weight.change?.let { c ->
                        Spacer(Modifier.size(10.dp))
                        val sign = if (c > 0) "+" else ""
                        Text(
                            text = "较上次 $sign${trimWeight(c)} kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                }
                if (weight.chartPoints.size >= 2) {
                    Spacer(Modifier.height(12.dp))
                    DetailedWeightChart(
                        points = weight.chartPoints,
                        color = HoneyDeep,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightRecordDialog(onConfirm: (Double) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("记录今天体重") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("体重") },
                suffix = { Text("kg") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { text.toDoubleOrNull()?.let(onConfirm) },
                enabled = text.toDoubleOrNull() != null,
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

private fun trimWeight(v: Double): String =
    if (v % 1.0 == 0.0) v.toInt().toString() else "%.1f".format(v)

@Preview(name = "训练日历", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun CalendarPreview() {
    val today = LocalDate.now()
    val start = today.withDayOfMonth(1)
    val days = (0 until 42).map { i ->
        val d = start.plusDays((i - 2).toLong())
        CalendarDay(
            date = d,
            inMonth = d.month == today.month,
            isToday = d == today,
            partColors = if (i % 5 == 0) listOf(com.fitlog.ui.screens.training.TrainingTarget.CHEST.color) else emptyList(),
            partAbbrev = if (i % 5 == 0) "胸" else null,
            holiday = null,
        )
    }
    FitLogTheme {
        CalendarContent(
            state = CalendarUiState(
                monthLabel = "${today.year}年${today.monthValue}月",
                days = days,
                monthCount = 8,
                selectedDate = today,
                selectedSummaries = listOf(CalendarDaySummary(1, "胸部", 52)),
                isLoading = false,
            ),
            weight = WeightSummary(
                current = 70.5,
                change = -0.3,
                points = listOf(72.0, 71.6, 71.8, 71.2, 70.9, 70.8, 70.5),
                chartPoints = listOf(
                    WeightChartPoint("6/1", 72.0),
                    WeightChartPoint("6/3", 71.6),
                    WeightChartPoint("6/5", 71.2),
                    WeightChartPoint("6/8", 70.5),
                ),
            ),
            memos = emptySet(),
            onPrev = {},
            onNext = {},
            onSelect = {},
            onAddWeight = {},
            onAddMemo = { _, _ -> },
            onOpenWorkout = {},
            onOpenWeight = {},
        )
    }
}
