/*
 * Copyright (C) 2025-2026 Hexis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.loc.hexis.shared.ui.habit.ui.component.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.habits.WeeklyTimePeriod
import com.loc.hexis.core.habits.WeeklyTimePeriod.Companion.toDisplayString
import com.loc.hexis.core.habits.WeeklyTimePeriod.Companion.toWeeks
import com.loc.hexis.shared.ui.habit.ui.component.AnalyticsCard
import com.loc.hexis.shared.ui.habit.ui.component.NotEnoughData
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun TrendLineChart(weeklyPointsHistory: List<Int>, modifier: Modifier = Modifier) {
    var selectedTimePeriod by rememberSaveable { mutableStateOf(WeeklyTimePeriod.MONTHS_2) }
    val visibleData =
        remember(selectedTimePeriod, weeklyPointsHistory) {
            weeklyPointsHistory.takeLast(selectedTimePeriod.toWeeks())
        }
    val maxVal =
        remember(visibleData) {
            visibleData.maxOrNull()?.coerceAtLeast(1)?.takeIf { visibleData.any { it > 0 } }
        }

    AnalyticsCard(
        title = stringResource(Res.string.points_trend),
        icon = Res.drawable.chart_data,
        modifier = modifier.heightIn(max = 400.dp),
    ) {
        if (maxVal != null) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                horizontalArrangement =
                    Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                WeeklyTimePeriod.entries.forEach { period ->
                    ToggleButton(
                        checked = period == selectedTimePeriod,
                        onCheckedChange = { selectedTimePeriod = period },
                        shapes =
                            when (period) {
                                WeeklyTimePeriod.MONTHS_2 ->
                                    ButtonGroupDefaults.connectedLeadingButtonShapes()
                                WeeklyTimePeriod.YEARS_1 ->
                                    ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                        modifier = Modifier.weight(1f),
                        colors =
                            ToggleButtonDefaults.toggleButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                    ) {
                        Text(text = period.toDisplayString())
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val textMeasurer = rememberTextMeasurer()
            val lineColor = MaterialTheme.colorScheme.primary
            val fillColor = lineColor.copy(alpha = 0.15f)
            val surfaceColor = MaterialTheme.colorScheme.surface
            val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
            val textStyle = MaterialTheme.typography.labelSmall.copy(fontFamily = flexFontRounded())

            Canvas(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val width = size.width
                val height = size.height
                val padding = 32.dp.toPx()
                val graphWidth = width - 2 * padding
                val graphHeight = height - 2 * padding

                // Grid lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = padding + (graphHeight / gridLines) * i
                    drawLine(
                        color = outlineVariantColor,
                        start = Offset(padding, y),
                        end = Offset(width - padding, y),
                        strokeWidth = 1f,
                    )
                }

                // Map data to pixel offsets
                val points =
                    visibleData.mapIndexed { index, value ->
                        val x =
                            padding +
                                (index.toFloat() / (visibleData.size - 1).coerceAtLeast(1)) *
                                    graphWidth
                        val y = height - padding - (value.toFloat() / maxVal) * graphHeight
                        Offset(x, y)
                    }

                if (points.size >= 2) {
                    // Gradient fill under curve
                    val fillPath =
                        Path().apply {
                            moveTo(points.first().x, height - padding)
                            points.forEachIndexed { i, point ->
                                if (i == 0) moveTo(point.x, point.y)
                                else {
                                    val prev = points[i - 1]
                                    val cp1x = prev.x + (point.x - prev.x) / 3f
                                    val cp1y = prev.y
                                    val cp2x = point.x - (point.x - prev.x) / 3f
                                    val cp2y = point.y
                                    cubicTo(cp1x, cp1y, cp2x, cp2y, point.x, point.y)
                                }
                            }
                            lineTo(points.last().x, height - padding)
                            close()
                        }
                    drawPath(path = fillPath, color = fillColor)

                    // Stroke path (smooth curve)
                    val strokePath =
                        Path().apply {
                            points.forEachIndexed { i, point ->
                                if (i == 0) moveTo(point.x, point.y)
                                else {
                                    val prev = points[i - 1]
                                    val cp1x = prev.x + (point.x - prev.x) / 3f
                                    val cp1y = prev.y
                                    val cp2x = point.x - (point.x - prev.x) / 3f
                                    val cp2y = point.y
                                    cubicTo(cp1x, cp1y, cp2x, cp2y, point.x, point.y)
                                }
                            }
                        }
                    drawPath(
                        path = strokePath,
                        color = lineColor,
                        style =
                            Stroke(
                                width = 4.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            ),
                    )

                    // Dots at each point
                    points.forEach { point ->
                        drawCircle(
                            color = lineColor,
                            center = Offset(point.x, point.y),
                            radius = 6.dp.toPx(),
                        )
                        drawCircle(
                            color = surfaceColor,
                            center = Offset(point.x, point.y),
                            radius = 3.dp.toPx(),
                        )
                    }
                } else if (points.size == 1) {
                    // Single point — just draw a dot
                    val point = points.first()
                    drawCircle(
                        color = lineColor,
                        center = Offset(point.x, point.y),
                        radius = 6.dp.toPx(),
                    )
                    drawCircle(
                        color = surfaceColor,
                        center = Offset(point.x, point.y),
                        radius = 3.dp.toPx(),
                    )
                }

                // Label latest value
                if (points.isNotEmpty()) {
                    val last = points.last()
                    val label = "${visibleData.last()}"
                    val textResult = textMeasurer.measure(text = label, style = textStyle)
                    drawText(
                        textLayoutResult = textResult,
                        topLeft =
                            Offset(
                                last.x - textResult.size.width / 2,
                                last.y - textResult.size.height - 8.dp.toPx(),
                            ),
                        color = lineColor,
                    )
                }
            }
        } else NotEnoughData()
    }
}
