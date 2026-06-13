package com.fitlog.ui.screens.home

/**
 * Stateless UI state for the home screen. Built by [HomeViewModel] from the current
 * user and this week's workouts; also used directly by @Preview with sample data.
 */
data class HomeUiState(
    val userId: Long = 0,
    val userName: String = "",
    val greeting: String = "你好",
    val greetingEmoji: String = "👋",
    val guideLine: String = "准备好开始今天的训练了吗？",
    val dailyQuote: String = "",
    val weekDays: List<WeekDayDot> = List(7) { WeekDayDot(trained = false, partAbbrev = null) },
    val weekWorkoutCount: Int = 0,
    val weekTotalMinutes: Int = 0,
    val recent: RecentWorkoutSummary? = null,
    val isLoading: Boolean = true,
)

/** One day in the read-only weekly strip (Mon..Sun order). */
data class WeekDayDot(
    val trained: Boolean,
    val partAbbrev: String?, // single-char part label, only when trained
)

/** Summary card for the most recent completed workout. */
data class RecentWorkoutSummary(
    val workoutId: Long,
    val name: String,       // e.g. "胸部训练"
    val dateLabel: String,  // e.g. "6月5日"
    val exerciseCount: Int,
    val setCount: Int,
    val minutes: Int,
)
