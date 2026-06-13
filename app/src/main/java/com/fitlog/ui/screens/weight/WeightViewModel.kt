package com.fitlog.ui.screens.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlog.data.local.entity.BodyWeight
import com.fitlog.data.prefs.UserPreferencesRepository
import com.fitlog.data.repository.TrainingRepository
import com.fitlog.data.repository.UserRepository
import com.fitlog.ui.components.WeightChartPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WeightEntryUi(val dateLabel: String, val kg: Double)

data class WeightUiState(
    val currentKg: Double? = null,
    val changeKg: Double? = null,
    val points: List<Double> = emptyList(),
    val chartPoints: List<WeightChartPoint> = emptyList(),
    val heightCm: Double? = null,
    val bmi: Double? = null,
    val bmiCategory: String = "",
    val history: List<WeightEntryUi> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class WeightViewModel(
    private val userRepository: UserRepository,
    private val trainingRepository: TrainingRepository,
    private val preferences: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<WeightUiState> =
        userRepository.currentUserId.flatMapLatest { uid ->
            if (uid == null) {
                flowOf(WeightUiState())
            } else {
                combine(
                    trainingRepository.bodyWeightFor(uid),
                    preferences.bodyData(uid),
                ) { list, body ->
                    val sorted = list.sortedBy { it.date }
                    val current = sorted.lastOrNull()?.weightKg
                    val prev = if (sorted.size >= 2) sorted[sorted.size - 2].weightKg else null
                    val bmi = if (current != null && body.heightCm != null && body.heightCm > 0) {
                        val h = body.heightCm / 100.0
                        current / (h * h)
                    } else {
                        null
                    }
                    WeightUiState(
                        currentKg = current,
                        changeKg = if (current != null && prev != null) current - prev else null,
                        points = sorted.takeLast(20).map { it.weightKg },
                        chartPoints = sorted.takeLast(10).map {
                            WeightChartPoint(
                                dateLabel = "${it.date.monthValue}/${it.date.dayOfMonth}",
                                weightKg = it.weightKg,
                            )
                        },
                        heightCm = body.heightCm,
                        bmi = bmi,
                        bmiCategory = bmi?.let(::bmiCategory) ?: "",
                        history = sorted.asReversed().take(20)
                            .map { WeightEntryUi("${it.date.monthValue}月${it.date.dayOfMonth}日", it.weightKg) },
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeightUiState())

    fun addWeight(kg: Double, date: LocalDate) {
        if (kg <= 0.0 || date.isAfter(LocalDate.now())) return
        viewModelScope.launch {
            val uid = userRepository.currentUserId.first() ?: return@launch
            trainingRepository.upsertBodyWeight(BodyWeight(userId = uid, date = date, weightKg = kg))
        }
    }
}

private fun bmiCategory(bmi: Double): String = when {
    bmi < 18.5 -> "偏瘦"
    bmi < 24.0 -> "正常"
    bmi < 28.0 -> "超重"
    else -> "肥胖"
}
