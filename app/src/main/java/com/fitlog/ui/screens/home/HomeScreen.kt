package com.fitlog.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitlog.ui.FitLogViewModelFactory
import com.fitlog.ui.components.WeightSparkline
import com.fitlog.ui.screens.weight.WeightUiState
import com.fitlog.ui.screens.weight.WeightViewModel
import com.fitlog.ui.theme.AvatarGradients
import com.fitlog.ui.theme.FitLogTheme
import com.fitlog.ui.theme.HoneyDeep
import com.fitlog.ui.theme.HoneyYellow
import kotlin.math.absoluteValue

/** Stateful entry: wires [HomeViewModel] to the stateless [HomeContent]. */
@Composable
fun HomeScreen(
    onStartTraining: () -> Unit,
    onViewRecentDetail: (Long) -> Unit = {},
    onOpenWeight: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = FitLogViewModelFactory.Factory),
    weightViewModel: WeightViewModel = viewModel(factory = FitLogViewModelFactory.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val weight by weightViewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        weight = weight,
        onStartTraining = onStartTraining,
        onViewRecentDetail = onViewRecentDetail,
        onOpenWeight = onOpenWeight,
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    weight: WeightUiState,
    onStartTraining: () -> Unit,
    onViewRecentDetail: (Long) -> Unit,
    onOpenWeight: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))
        GreetingHeader(state)

        Spacer(Modifier.height(48.dp))
        StartTrainingButton(onClick = onStartTraining)

        Spacer(Modifier.height(44.dp))
        WeeklyStrip(state)

        Spacer(Modifier.height(36.dp))
        RecentWorkoutCard(
            recent = state.recent,
            onClick = { state.recent?.let { onViewRecentDetail(it.workoutId) } },
        )

        Spacer(Modifier.height(16.dp))
        WeightMiniCard(weight = weight, onClick = onOpenWeight)

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun WeightMiniCard(weight: WeightUiState, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                Text("体重", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = weight.currentKg?.let { "${if (it % 1.0 == 0.0) it.toInt().toString() else "%.1f".format(it)} kg" } ?: "记录体重 ›",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            if (weight.points.size >= 2) {
                WeightSparkline(
                    points = weight.points,
                    color = HoneyDeep,
                    modifier = Modifier.width(96.dp).height(36.dp),
                )
            }
        }
    }
}

@Composable
private fun GreetingHeader(state: HomeUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(seed = state.userId, name = state.userName, size = 48)
            Spacer(Modifier.size(12.dp))
            Text(
                text = "${state.greetingEmoji} ${state.greeting}，${state.userName}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = state.guideLine,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (state.dailyQuote.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = state.dailyQuote,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StartTrainingButton(onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        // Soft background glow, kept subtle so it never hurts readability.
        Box(
            modifier = Modifier
                .size(256.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(HoneyYellow.copy(alpha = 0.30f), Color.Transparent),
                    ),
                    shape = CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(196.dp)
                .shadow(20.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(HoneyYellow, HoneyDeep)))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "开始",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = "今天的训练",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.95f),
                )
            }
        }
    }
}

@Composable
private fun WeeklyStrip(state: HomeUiState) {
    val quiet = MaterialTheme.colorScheme.onSurfaceVariant
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "本周训练",
            style = MaterialTheme.typography.titleSmall,
            color = quiet,
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            state.weekDays.forEach { day ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (day.trained) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, quiet.copy(alpha = 0.45f), CircleShape),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.height(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = day.partAbbrev ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = quiet,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "已训练 ${state.weekWorkoutCount} 次 · 共 ${state.weekTotalMinutes} 分钟",
            style = MaterialTheme.typography.bodyMedium,
            color = quiet,
        )
    }
}

@Composable
private fun RecentWorkoutCard(
    recent: RecentWorkoutSummary?,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "最近一次训练",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            if (recent == null) {
                Text(
                    text = "还没有训练记录，点上方按钮开始吧",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = recent.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${recent.dateLabel} · ${recent.exerciseCount}个动作 · ${recent.setCount}组 · 共${recent.minutes}分钟",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "查看详情 ›",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = HoneyDeep,
                    modifier = Modifier.clickable(onClick = onClick),
                )
            }
        }
    }
}

@Composable
private fun UserAvatar(seed: Long, name: String, size: Int) {
    val gradient = AvatarGradients[((seed % AvatarGradients.size).toInt().absoluteValue)]
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

// ---------------------------------------------------------------------------
// Previews — render live in Android Studio, no build/install/emulator needed.
// ---------------------------------------------------------------------------

private val sampleWeek = listOf(
    WeekDayDot(true, "胸"),
    WeekDayDot(false, null),
    WeekDayDot(true, "背"),
    WeekDayDot(false, null),
    WeekDayDot(true, "臂"),
    WeekDayDot(false, null),
    WeekDayDot(false, null),
)

@Preview(name = "首页 · 有数据", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HomePopulatedPreview() {
    FitLogTheme {
        HomeContent(
            state = HomeUiState(
                userId = 2,
                userName = "Jason",
                greeting = "早上好",
                greetingEmoji = "☀️",
                dailyQuote = "保持节奏，每一次记录都算数。",
                weekDays = sampleWeek,
                weekWorkoutCount = 3,
                weekTotalMinutes = 145,
                recent = RecentWorkoutSummary(1, "胸部训练", "6月5日", 5, 16, 52),
                isLoading = false,
            ),
            weight = WeightUiState(currentKg = 70.5, points = listOf(72.0, 71.5, 70.9, 70.5)),
            onStartTraining = {},
            onViewRecentDetail = {},
            onOpenWeight = {},
        )
    }
}

@Preview(name = "首页 · 空状态", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HomeEmptyPreview() {
    FitLogTheme {
        HomeContent(
            state = HomeUiState(
                userId = 1,
                userName = "小王",
                greeting = "晚上好",
                greetingEmoji = "🌙",
                dailyQuote = "不必完美，只要开始。",
                isLoading = false,
            ),
            weight = WeightUiState(currentKg = 70.5, points = listOf(72.0, 71.5, 70.9, 70.5)),
            onStartTraining = {},
            onViewRecentDetail = {},
            onOpenWeight = {},
        )
    }
}

@Preview(name = "首页 · 长名字", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HomeLongNamePreview() {
    FitLogTheme {
        HomeContent(
            state = HomeUiState(
                userId = 4,
                userName = "训练超级认真的大力士",
                greeting = "下午好",
                greetingEmoji = "🌤️",
                dailyQuote = "你已经比昨天更进一步。",
                weekDays = sampleWeek,
                weekWorkoutCount = 3,
                weekTotalMinutes = 145,
                recent = RecentWorkoutSummary(2, "背部+二头训练", "6月6日", 7, 21, 68),
                isLoading = false,
            ),
            weight = WeightUiState(currentKg = 70.5, points = listOf(72.0, 71.5, 70.9, 70.5)),
            onStartTraining = {},
            onViewRecentDetail = {},
            onOpenWeight = {},
        )
    }
}
