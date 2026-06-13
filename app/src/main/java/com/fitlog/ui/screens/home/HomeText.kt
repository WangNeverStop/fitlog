package com.fitlog.ui.screens.home

import java.time.LocalDate
import java.time.LocalTime

/**
 * Time-of-day greeting + emoji. The exact hour boundaries are a sensible default
 * pending the open question in PROJECT_LOG ("早上/下午/晚上的具体时间分界").
 */
fun greetingFor(time: LocalTime = LocalTime.now()): Pair<String, String> = when (time.hour) {
    in 5..10 -> "早上好" to "☀️"
    in 11..17 -> "下午好" to "🌤️"
    else -> "晚上好" to "🌙"
}

// Local rotating motivational lines. Stable within a calendar day (indexed by epoch day),
// so reopening the app on the same day does not change the line.
private val dailyQuotes = listOf(
    "保持节奏，每一次记录都算数。",
    "今天的努力，是明天的底气。",
    "不必完美，只要开始。",
    "稳稳地练，慢慢地强。",
    "你已经比昨天更进一步。",
    "认真对待每一组。",
    "坚持，是最酷的天赋。",
)

fun dailyQuote(date: LocalDate = LocalDate.now()): String =
    dailyQuotes[(date.toEpochDay() % dailyQuotes.size).toInt()]
