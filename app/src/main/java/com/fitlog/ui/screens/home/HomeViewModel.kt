package com.fitlog.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlog.data.local.entity.Workout
import com.fitlog.data.repository.TrainingRepository
import com.fitlog.data.repository.UserRepository
import com.fitlog.ui.screens.training.TrainingTarget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Builds [HomeUiState] for the current user from real data: greeting, this week's
 * training dots (with single-char part labels) and totals, and the most recent
 * completed workout summary.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val userRepository: UserRepository,
    private val trainingRepository: TrainingRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        userRepository.currentUserId.flatMapLatest { userId ->
            if (userId == null) {
                flowOf(HomeUiState(isLoading = false))
            } else {
                val today = LocalDate.now()
                val weekStart = today.with(DayOfWeek.MONDAY)
                val weekEnd = weekStart.plusDays(6)
                combine(
                    flow { emit(userRepository.getUser(userId)) },
                    trainingRepository.workoutsBetween(userId, weekStart, weekEnd),
                ) { user, week -> user to week }
                    .map { (user, week) ->
                        val recent = buildRecent(userId)
                        buildState(userId, user?.name ?: "", week, weekStart, today, recent)
                    }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            HomeUiState(),
        )

    private suspend fun buildRecent(userId: Long): RecentWorkoutSummary? {
        val w = trainingRepository.lastCompletedAny(userId) ?: return null
        val exCount = trainingRepository.exerciseCount(w.id)
        val setCount = trainingRepository.completedSetCount(w.id)
        val minutes = workoutMinutes(w)
        val name = targetDisplayName(w.targetName)?.plus("训练") ?: "训练"
        val dateLabel = "${w.date.monthValue}月${w.date.dayOfMonth}日"
        return RecentWorkoutSummary(
            workoutId = w.id,
            name = name,
            dateLabel = dateLabel,
            exerciseCount = exCount,
            setCount = setCount,
            minutes = minutes,
        )
    }
}

private fun buildState(
    userId: Long,
    userName: String,
    weekWorkouts: List<Workout>,
    weekStart: LocalDate,
    today: LocalDate,
    recent: RecentWorkoutSummary?,
): HomeUiState {
    val (greeting, emoji) = greetingFor()
    val completed = weekWorkouts.filter { it.completed }
    val byDay = completed.groupBy { it.date.dayOfWeek }

    val dots = (0..6).map { i ->
        val day = weekStart.plusDays(i.toLong()).dayOfWeek
        val dayWorkouts = byDay[day].orEmpty()
        val abbrev = dayWorkouts.firstNotNullOfOrNull { targetAbbrev(it.targetName) }
        WeekDayDot(trained = dayWorkouts.isNotEmpty(), partAbbrev = abbrev)
    }
    val minutes = completed.sumOf { workoutMinutes(it) }

    return HomeUiState(
        userId = userId,
        userName = userName,
        greeting = greeting,
        greetingEmoji = emoji,
        dailyQuote = dailyQuote(today),
        weekDays = dots,
        weekWorkoutCount = completed.size,
        weekTotalMinutes = minutes,
        recent = recent,
        isLoading = false,
    )
}

private fun workoutMinutes(w: Workout): Int {
    val s = w.startTime
    val e = w.endTime
    return if (s != null && e != null && e > s) ((e - s) / 60_000L).toInt() else 0
}

private fun targetAbbrev(targetName: String?): String? =
    targetName?.let { runCatching { TrainingTarget.valueOf(it).abbrev }.getOrNull() }

private fun targetDisplayName(targetName: String?): String? =
    targetName?.let { runCatching { TrainingTarget.valueOf(it).displayName }.getOrNull() }
