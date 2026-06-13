package com.fitlog.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

data class WeightChartPoint(
    val dateLabel: String,
    val weightKg: Double,
)

/**
 * Detailed chart for the Weight and Calendar pages. Home intentionally keeps using the
 * compact sparkline, while this component shows axes, dates and point values.
 */
@Composable
fun DetailedWeightChart(
    points: List<WeightChartPoint>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (points.size < 2) {
        Text(
            text = "至少记录两次体重后显示趋势图",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    val visible = points.takeLast(10)
    val rawMin = visible.minOf { it.weightKg }
    val rawMax = visible.maxOf { it.weightKg }
    val padding = max((rawMax - rawMin) * 0.18, 0.5)
    val minValue = rawMin - padding
    val maxValue = rawMax + padding
    val midValue = (minValue + maxValue) / 2.0
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth().height(188.dp)) {
            Column(
                modifier = Modifier.width(46.dp).fillMaxHeight().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
            ) {
                AxisLabel(maxValue)
                AxisLabel(midValue)
                AxisLabel(minValue)
            }
            Spacer(Modifier.width(8.dp))
            Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val top = 18.dp.toPx()
                val bottom = size.height - 18.dp.toPx()
                val chartHeight = bottom - top
                val stepX = size.width / (visible.size - 1)
                val range = maxValue - minValue

                listOf(top, top + chartHeight / 2f, bottom).forEach { y ->
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }

                val offsets = visible.mapIndexed { index, point ->
                    Offset(
                        x = index * stepX,
                        y = bottom - (((point.weightKg - minValue) / range).toFloat() * chartHeight),
                    )
                }
                offsets.zipWithNext().forEach { (start, end) ->
                    drawLine(color, start, end, strokeWidth = 3.dp.toPx())
                }
                offsets.forEach { point ->
                    drawCircle(Color.White, radius = 5.dp.toPx(), center = point)
                    drawCircle(color, radius = 3.2.dp.toPx(), center = point)
                }

                val textPaint = Paint().apply {
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                    textSize = 10.sp.toPx()
                    this.color = android.graphics.Color.argb(
                        (labelColor.alpha * 255).toInt(),
                        (labelColor.red * 255).toInt(),
                        (labelColor.green * 255).toInt(),
                        (labelColor.blue * 255).toInt(),
                    )
                }
                val labelEvery = if (visible.size <= 6) 1 else 2
                visible.forEachIndexed { index, point ->
                    if (index % labelEvery == 0 || index == visible.lastIndex) {
                        val y = (offsets[index].y - 8.dp.toPx()).coerceAtLeast(11.sp.toPx())
                        drawContext.canvas.nativeCanvas.drawText(
                            formatWeight(point.weightKg),
                            offsets[index].x,
                            y,
                            textPaint,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 54.dp, top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val labels = listOf(
                visible.first().dateLabel,
                visible[visible.lastIndex / 2].dateLabel,
                visible.last().dateLabel,
            )
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                )
            }
        }
        Text(
            text = "体重（kg）",
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun AxisLabel(value: Double) {
    Text(
        text = formatWeight(value),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun formatWeight(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
