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
import androidx.compose.ui.graphics.StrokeCap
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
                val padL = 40.dp.toPx()
                val padR = 16.dp.toPx()
                val padT = 8.dp.toPx()
                val padB = 24.dp.toPx()
                val gW = width - padL - padR
                val gH = height - padT - padB

                // Grid lines + Y-axis labels
                val gridCount = 3
                for (i in 0..gridCount) {
                    val y = padT + (gH / gridCount) * i
                    drawLine(
                        color = outlineVariantColor.copy(alpha = 0.4f),
                        start = Offset(padL, y),
                        end = Offset(width - padR, y),
                        strokeWidth = 1f,
                    )
                    val labelVal = maxVal - (maxVal / gridCount) * i
                    val labelResult =
                        textMeasurer.measure(
                            text = "$labelVal",
                            style = textStyle,
                        )
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = Offset(0f, y - labelResult.size.height / 2f),
                        color = outlineVariantColor,
                    )
                }

                // Map data
                val pts =
                    visibleData.mapIndexed { index, value ->
                        val x = padL + (index.toFloat() / (visibleData.size - 1).coerceAtLeast(1)) * gW
                        val y = height - padB - (value.toFloat() / maxVal) * gH
                        Offset(x, y)
                    }

                if (pts.size >= 2) {
                    // Smooth bezier path (Catmull-Rom)
                    fun smoothPath(): Path =
                        Path().apply {
                            moveTo(pts[0].x, pts[0].y)
                            for (i in 0 until pts.size - 1) {
                                val p0 = if (i > 0) pts[i - 1] else pts[i]
                                val p1 = pts[i]
                                val p2 = pts[i + 1]
                                val p3 = if (i < pts.size - 2) pts[i + 2] else pts[i + 1]
                                cubicTo(
                                    p1.x + (p2.x - p0.x) / 6f,
                                    p1.y + (p2.y - p0.y) / 6f,
                                    p2.x - (p3.x - p1.x) / 6f,
                                    p2.y - (p3.y - p1.y) / 6f,
                                    p2.x, p2.y,
                                )
                            }
                        }

                    val sp = smoothPath()

                    // Gradient fill
                    val fillPath =
                        Path().apply {
                            addPath(sp)
                            lineTo(pts.last().x, height - padB)
                            lineTo(pts.first().x, height - padB)
                            close()
                        }
                    drawPath(path = fillPath, color = lineColor.copy(alpha = 0.1f))

                    // Glow
                    drawPath(
                        path = sp,
                        color = lineColor.copy(alpha = 0.2f),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                    )

                    // Main line
                    drawPath(
                        path = sp,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                    )

                    // Dots
                    pts.forEach { pt ->
                        drawCircle(color = lineColor, center = pt, radius = 5.dp.toPx())
                        drawCircle(color = surfaceColor, center = pt, radius = 2.5.dp.toPx())
                    }

                    // Last value label
                    val lastPt = pts.last()
                    val label = "${visibleData.last()}"
                    val labelResult = textMeasurer.measure(text = label, style = textStyle)
                    val labelX =
                        (lastPt.x - labelResult.size.width / 2f)
                            .coerceIn(padL, width - padR - labelResult.size.width)
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = Offset(labelX, lastPt.y - labelResult.size.height - 6.dp.toPx()),
                        color = lineColor,
                    )
                } else if (pts.size == 1) {
                    val pt = pts.first()
                    drawCircle(color = lineColor, center = pt, radius = 5.dp.toPx())
                    drawCircle(color = surfaceColor, center = pt, radius = 2.5.dp.toPx())
                }
            }
        } else NotEnoughData()
    }
}
