package com.fitlog.ui.screens.training

import androidx.compose.ui.graphics.Color

/**
 * The fixed list of today's training targets (PROJECT_LOG 1.0.1). Full names are used
 * here ("手臂", not "臂"); the single-char [abbrev] is for the home weekly dots.
 *
 * [isFree] marks 有氧 / 暂无·自由训练, which skip the "reuse last workout" routing and
 * go straight to action selection.
 */
enum class TrainingTarget(
    val displayName: String,
    val abbrev: String,
    val color: Color,
    val isFree: Boolean,
) {
    CHEST("胸部", "胸", Color(0xFFE57373), false),
    BACK("背部", "背", Color(0xFF64B5F6), false),
    SHOULDER("肩部", "肩", Color(0xFFFFB74D), false),
    ARM("手臂", "臂", Color(0xFFBA68C8), false),
    ABS("腹部", "腹", Color(0xFF4DB6AC), false),
    LEG("腿部", "腿", Color(0xFF81C784), false),
    GLUTE("臀部", "臀", Color(0xFFF06292), false),
    CARDIO("有氧", "氧", Color(0xFF4DD0E1), true),
    FREE("暂无/自由训练", "自", Color(0xFF90A4AE), true);

    companion object {
        /** Body parts + cardio, shown in the 2-column grid. */
        val gridItems: List<TrainingTarget> =
            listOf(CHEST, BACK, SHOULDER, ARM, ABS, LEG, GLUTE, CARDIO)
    }
}
