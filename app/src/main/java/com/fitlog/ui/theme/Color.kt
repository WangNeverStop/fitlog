package com.fitlog.ui.theme

import androidx.compose.ui.graphics.Color

// Warm "honey + ivory" palette aligned with the agreed visual language
// (bright, simple, warm). Placeholder values pending the final UI prototype.
val HoneyYellow = Color(0xFFF4C04E)
val HoneyDeep = Color(0xFFE0A327)
val IvoryBackground = Color(0xFFFFFBF3)
val WarmSurface = Color(0xFFFFFFFF)
val WarmSurfaceVariant = Color(0xFFF3ECE0)
val WarmTextPrimary = Color(0xFF3A352D)
val WarmTextSecondary = Color(0xFF8A8175)

// Per-profile avatar gradients (warm, distinct). Indexed by user id.
val AvatarGradients: List<List<Color>> = listOf(
    listOf(Color(0xFFF6B24E), Color(0xFFF0833C)),
    listOf(Color(0xFF6FB1FF), Color(0xFF4E7CF0)),
    listOf(Color(0xFF7FC6A6), Color(0xFF4EA07A)),
    listOf(Color(0xFFF58FA8), Color(0xFFE85F86)),
    listOf(Color(0xFFB99CF0), Color(0xFF8A6BE0)),
    listOf(Color(0xFF6FD0C8), Color(0xFF3FA89E)),
)
