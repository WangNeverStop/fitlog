package com.fitlog.ui.screens.calendar

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlog.data.local.entity.BodyWeight
import com.fitlog.data.local.entity.Workout
import com.fitlog.data.prefs.UserPreferencesRepository
import com.fitlog.data.repository.TrainingRepository
import com.fitlog.data.repository.UserRepository
import com.fitlog.ui.components.WeightChartPoint
import com.fitlog.ui.screens.training.TrainingTarget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

data class CalendarDay(
    val date: LocalDate,
    val inMonth: Boolean,
    val isToday: Boolean,
    val partColors: List<Color>,
    val partAbbrev: String?,
    val holiday: String?,
)

data class CalendarDaySummary(
    val workoutId: Long,
    val targetName: String,
    val minutes: Int,
)

data class CalendarUiState(
    val monthLabel: String = "",
    val days: List<CalendarDay> = emptyList(),
    val monthCount: Int = 0,
    val selectedDate: LocalDate? = null,
    val selectedSummaries: List<CalendarDaySummary> = emptyList(),
    val isLoading: Boolean = true,
)

data class WeightSummary(
    val current: Double? = null,
    val change: Double? = null,
    val points: List<Double> = emptyList(),
    val chartPoints: List<WeightChartPoint> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val userRepository: UserRepository,
    private val trainingRepository: TrainingRepository,
    private val preferences: UserPreferencesRepository,
) : ViewModel() {

    private val month = MutableStateFlow(YearMonth.now())
    private val selected = MutableStateFlow<LocalDate?>(null)

    /** Current user's calendar memos, each encoded "MM-dd|text" (recur yearly). */
    val memos: StateFlow<Set<String>> =
        userRepository.currentUserId.flatMapLatest { uid ->
            if (uid == null) flowOf(emptySet()) else preferences.memos(uid)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun addMemo(date: LocalDate, text: String) {
        viewModelScope.launch {
            val uid = userRepository.currentUserId.first() ?: return@launch
            preferences.addMemo(uid, "%02d-%02d".format(date.monthValue, date.dayOfMonth), text)
        }
    }

    val uiState: StateFlow<CalendarUiState> =
        userRepository.currentUserId.flatMapLatest { userId ->
            if (userId == null) {
                flowOf(CalendarUiState(isLoading = false))
            } else {
                month.flatMapLatest { ym ->
                    val gridStart = gridStart(ym)
                    val gridEnd = gridStart.plusDays(41)
                    combine(
                        trainingRepository.workoutsBetween(userId, gridStart, gridEnd),
                        selected,
                    ) { workouts, sel ->
                        buildState(ym, workouts, sel)
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    val weight: StateFlow<WeightSummary> =
        userRepository.currentUserId.flatMapLatest { uid ->
            if (uid == null) {
                flowOf(WeightSummary())
            } else {
                trainingRepository.bodyWeightFor(uid).map { list ->
                    val sorted = list.sortedBy { it.date }
                    val current = sorted.lastOrNull()?.weightKg
                    val prev = if (sorted.size >= 2) sorted[sorted.size - 2].weightKg else null
                    WeightSummary(
                        current = current,
                        change = if (current != null && prev != null) current - prev else null,
                        points = sorted.takeLast(14).map { it.weightKg },
                        chartPoints = sorted.takeLast(7).map {
                            WeightChartPoint(
                                dateLabel = "${it.date.monthValue}/${it.date.dayOfMonth}",
                                weightKg = it.weightKg,
                            )
                        },
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeightSummary())

    fun addWeight(kg: Double) {
        viewModelScope.launch {
            val uid = userRepository.currentUserId.first() ?: return@launch
            trainingRepository.upsertBodyWeight(
                BodyWeight(userId = uid, date = LocalDate.now(), weightKg = kg),
            )
        }
    }

    fun prevMonth() {
        month.value = month.value.minusMonths(1)
    }

    fun nextMonth() {
        month.value = month.value.plusMonths(1)
    }

    fun selectDate(date: LocalDate) {
        selected.value = if (selected.value == date) null else date
    }
}

private fun gridStart(ym: YearMonth): LocalDate =
    ym.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

private fun buildState(ym: YearMonth, workouts: List<Workout>, selected: LocalDate?): CalendarUiState {
    val today = LocalDate.now()
    val completed = workouts.filter { it.completed }
    val byDate = completed.groupBy { it.date }
    val start = gridStart(ym)

    val days = (0 until 42).map { i ->
        val d = start.plusDays(i.toLong())
        val dayWorkouts = byDate[d].orEmpty()
        CalendarDay(
            date = d,
            inMonth = YearMonth.from(d) == ym,
            isToday = d == today,
            partColors = dayWorkouts.mapNotNull { targetColor(it.targetName) },
            partAbbrev = dayWorkouts.firstNotNullOfOrNull { targetAbbrev(it.targetName) },
            holiday = holidayFor(d),
        )
    }

    val monthCount = completed.count { YearMonth.from(it.date) == ym }
    val selSummaries = selected?.let { sel ->
        byDate[sel].orEmpty().map {
            CalendarDaySummary(
                workoutId = it.id,
                targetName = targetDisplay(it.targetName),
                minutes = workoutMinutes(it),
            )
        }
    }.orEmpty()

    return CalendarUiState(
        monthLabel = "${ym.year}年${ym.monthValue}月",
        days = days,
        monthCount = monthCount,
        selectedDate = selected,
        selectedSummaries = selSummaries,
        isLoading = false,
    )
}

private fun workoutMinutes(w: Workout): Int {
    val s = w.startTime
    val e = w.endTime
    return if (s != null && e != null && e > s) ((e - s) / 60_000L).toInt() else 0
}

private fun targetColor(name: String?): Color? =
    name?.let { runCatching { TrainingTarget.valueOf(it).color }.getOrNull() }

private fun targetAbbrev(name: String?): String? =
    name?.let { runCatching { TrainingTarget.valueOf(it).abbrev }.getOrNull() }

private fun targetDisplay(name: String?): String =
    name?.let { runCatching { TrainingTarget.valueOf(it).displayName }.getOrNull() } ?: "自由训练"

// Cell shows the rest-day short name in red; adjusted workdays (班) are surfaced only
// in the selected-day panel via Holidays2026.forDate.
private fun holidayFor(d: LocalDate): String? =
    Holidays2026.forDate(d)?.takeUnless { it.isWorkday }?.shortName
