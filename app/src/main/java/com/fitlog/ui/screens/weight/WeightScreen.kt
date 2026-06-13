package com.fitlog.ui.screens.weight

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitlog.ui.FitLogViewModelFactory
import com.fitlog.ui.components.DetailedWeightChart
import com.fitlog.ui.components.WeightChartPoint
import com.fitlog.ui.theme.FitLogTheme
import com.fitlog.ui.theme.HoneyDeep
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun WeightScreen(
    onBack: () -> Unit,
    viewModel: WeightViewModel = viewModel(factory = FitLogViewModelFactory.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    WeightContent(state = state, onBack = onBack, onAddWeight = viewModel::addWeight)
}

@Composable
fun WeightContent(
    state: WeightUiState,
    onBack: () -> Unit,
    onAddWeight: (Double, LocalDate) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

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
            Text("体重趋势", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
            Text("＋ 记录", style = MaterialTheme.typography.labelLarge, color = HoneyDeep, modifier = Modifier.clickable { showDialog = true })
        }

        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (state.currentKg == null) {
                    Text("还没有体重记录，点「＋ 记录」添加。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${trim(state.currentKg)} kg", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        state.changeKg?.let { c ->
                            Spacer(Modifier.height(0.dp))
                            Text("  较上次 ${if (c > 0) "+" else ""}${trim(c)} kg", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (state.bmi != null) {
                        Spacer(Modifier.height(8.dp))
                        Text("BMI 指数：${trim(state.bmi)}   ${state.bmiCategory}", style = MaterialTheme.typography.bodyMedium, color = HoneyDeep)
                    } else {
                        Spacer(Modifier.height(8.dp))
                        Text("填写身高后可计算 BMI（在「我的 → 身体资料」）", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (state.chartPoints.size >= 2) {
                        Spacer(Modifier.height(14.dp))
                        DetailedWeightChart(
                            points = state.chartPoints,
                            color = HoneyDeep,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        if (state.history.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text("历史记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            state.history.forEach { e ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(e.dateLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${trim(e.kg)} kg", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showDialog) {
        var text by remember { mutableStateOf("") }
        var selectedDate by remember { mutableStateOf(LocalDate.now()) }
        val context = LocalContext.current
        val datePicker = remember(context, selectedDate) {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    selectedDate = LocalDate.of(year, month + 1, day)
                },
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth,
            ).apply {
                datePicker.maxDate = LocalDate.now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }
        }
        val kg = text.toDoubleOrNull()
        val canSave = kg?.let { it > 0.0 } == true && !selectedDate.isAfter(LocalDate.now())
        AlertDialog(
            onDismissRequest = { showDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("记录体重") },
            text = {
                Column {
                    Text(
                        text = "日期：${formatDate(selectedDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HoneyDeep,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePicker.show() }
                            .padding(vertical = 10.dp),
                    )
                    Text(
                        text = if (selectedDate == LocalDate.now()) {
                            "今天"
                        } else {
                            "可补录历史日期；同一天再次保存会覆盖原记录"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("体重") },
                        suffix = { Text("kg") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        kg?.let {
                            onAddWeight(it, selectedDate)
                            showDialog = false
                        }
                    },
                    enabled = canSave,
                ) {
                    Text("保存")
                }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("取消") } },
        )
    }
}

private fun trim(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else "%.1f".format(v)

private fun formatDate(date: LocalDate): String =
    "${date.year}年${date.monthValue}月${date.dayOfMonth}日"

@Preview(name = "体重/BMI", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun WeightPreview() {
    FitLogTheme {
        WeightContent(
            state = WeightUiState(
                currentKg = 70.5,
                changeKg = -0.3,
                points = listOf(72.0, 71.6, 71.8, 71.2, 70.9, 70.8, 70.5),
                chartPoints = listOf(
                    WeightChartPoint("6/1", 72.0),
                    WeightChartPoint("6/3", 71.6),
                    WeightChartPoint("6/5", 71.2),
                    WeightChartPoint("6/8", 70.5),
                ),
                heightCm = 175.0,
                bmi = 23.0,
                bmiCategory = "正常",
                history = listOf(WeightEntryUi("6月8日", 70.5), WeightEntryUi("6月6日", 70.8)),
            ),
            onBack = {},
            onAddWeight = { _, _ -> },
        )
    }
}
