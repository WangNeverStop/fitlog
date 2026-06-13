package com.fitlog.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/** A tiny line chart of recent weight points. Reused on Home / Weight / Calendar. */
@Composable
fun WeightSparkline(points: List<Double>, color: Color, modifier: Modifier = Modifier) {
    if (points.size < 2) return
    val min = points.min()
    val max = points.max()
    val range = (max - min).takeIf { it > 0.0 } ?: 1.0
    Canvas(modifier = modifier) {
        val stepX = size.width / (points.size - 1)
        val offsets = points.mapIndexed { i, v ->
            Offset(i * stepX, size.height - ((v - min) / range).toFloat() * size.height)
        }
        for (i in 0 until offsets.size - 1) {
            drawLine(color = color, start = offsets[i], end = offsets[i + 1], strokeWidth = 5f)
        }
    }
}
