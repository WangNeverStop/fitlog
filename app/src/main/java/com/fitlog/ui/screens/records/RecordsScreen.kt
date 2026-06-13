package com.fitlog.ui.screens.records

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitlog.ui.FitLogViewModelFactory
import com.fitlog.ui.theme.FitLogTheme
import com.fitlog.ui.theme.HoneyDeep

@Composable
fun RecordsScreen(
    onOpen: (Long) -> Unit,
    onAddHistory: () -> Unit,
    viewModel: RecordsViewModel = viewModel(factory = FitLogViewModelFactory.Factory),
) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    RecordsContent(records = records, onOpen = onOpen, onAddHistory = onAddHistory)
}

@Composable
fun RecordsContent(
    records: List<RecordItem>,
    onOpen: (Long) -> Unit,
    onAddHistory: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "训练记录",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "＋ 补录历史训练",
                style = MaterialTheme.typography.labelLarge,
                color = HoneyDeep,
                modifier = Modifier.clickable(onClick = onAddHistory).padding(vertical = 8.dp),
            )
        }
        Spacer(Modifier.height(12.dp))

        if (records.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "还没有训练记录\n可以完成一次训练，或补录以前的训练经历",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
            ) {
                items(records, key = { it.id }) { item ->
                    RecordCard(item = item, onClick = { onOpen(item.id) })
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun RecordCard(item: RecordItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${item.dateLabel} · 共 ${item.minutes} 分钟",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(text = "查看详情 ›", style = MaterialTheme.typography.labelLarge, color = HoneyDeep)
        }
    }
}

@Preview(name = "训练记录", showBackground = true, widthDp = 360, heightDp = 700)
@Composable
private fun RecordsPreview() {
    FitLogTheme {
        RecordsContent(
            records = listOf(
                RecordItem(1, "胸部训练", "6月8日", 52),
                RecordItem(2, "背部训练", "6月6日", 47),
                RecordItem(3, "腿部训练", "6月4日", 63),
            ),
            onOpen = {},
            onAddHistory = {},
        )
    }
}
