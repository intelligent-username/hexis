package com.loc.hexis.shared.ui.habit.ui.component.stats

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.habits.WeeklyTimePeriod
import com.loc.hexis.core.habits.WeeklyTimePeriod.Companion.toDisplayString
import com.loc.hexis.core.habits.WeeklyTimePeriod.Companion.toWeeks
import com.loc.hexis.shared.ui.habit.ui.component.AnalyticsCard
import com.loc.hexis.shared.ui.habit.ui.component.NotEnoughData
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.*
import kotlin.math.abs
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

    // Draw-in animation: line traces itself left to right
    val drawProgress = remember(selectedTimePeriod) { Animatable(0f) }
    LaunchedEffect(selectedTimePeriod) {
        drawProgress.snapTo(0f)
        drawProgress.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
    }

    // Touch scrubbing
    var touchX by remember { mutableFloatStateOf(-1f) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    AnalyticsCard(
        title = stringResource(Res.string.points_trend),
        icon = Res.drawable.chart_data,
        modifier = modifier.heightIn(max = 420.dp),
    ) {
        if (maxVal != null) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement =
                    Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                WeeklyTimePeriod.entries.forEach { period ->
                    ToggleButton(
                        checked = period == selectedTimePeriod,
                        onCheckedChange = {
                            selectedTimePeriod = period
                            touchX = -1f
                            selectedIndex = -1
                        },
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
                        Text(text = period.toDisplayString(), fontFamily = flexFontRounded())
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tooltip: shows selected point value, or is empty
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(28.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                val tooltipVal =
                    if (selectedIndex >= 0 && selectedIndex < visibleData.size)
                        visibleData[selectedIndex]
                    else null
                if (tooltipVal != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 0.dp,
                    ) {
                        Text(
                            text = "$tooltipVal pts · week ${selectedIndex + 1}",
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = flexFontRounded(),
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val lineColor = MaterialTheme.colorScheme.primary
            val surfaceColor = MaterialTheme.colorScheme.surface
            val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
            val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
            val textMeasurer = rememberTextMeasurer()
            val labelStyle =
                MaterialTheme.typography.labelSmall.copy(fontFamily = flexFontRounded())
            val progress = drawProgress.value

            Canvas(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(210.dp)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .pointerInput(visibleData) {
                            detectTapGestures { offset -> touchX = offset.x }
                        }
            ) {
                val w = size.width
                val h = size.height
                val padL = 36.dp.toPx()
                val padR = 12.dp.toPx()
                val padT = 8.dp.toPx()
                val padB = 24.dp.toPx()
                val gW = w - padL - padR
                val gH = h - padT - padB
                val n = visibleData.size

                fun xOf(i: Int) = padL + (i.toFloat() / (n - 1).coerceAtLeast(1)) * gW
                fun yOf(v: Int) = h - padB - (v.toFloat() / maxVal) * gH

                val pts = visibleData.mapIndexed { i, v -> Offset(xOf(i), yOf(v)) }

                // Resolve scrubbing index
                if (touchX >= 0f && n > 1) {
                    selectedIndex = pts.indices.minByOrNull { abs(pts[it].x - touchX) } ?: -1
                }

                // Dashed grid lines
                val gridCount = 4
                for (i in 0..gridCount) {
                    val y = padT + (gH / gridCount) * i
                    drawLine(
                        color = outlineVariantColor.copy(alpha = 0.3f),
                        start = Offset(padL, y),
                        end = Offset(w - padR, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)),
                    )
                    val labelVal = maxVal - (maxVal / gridCount) * i
                    val labelResult = textMeasurer.measure("$labelVal", style = labelStyle)
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = Offset(0f, y - labelResult.size.height / 2f),
                        color = outlineVariantColor,
                    )
                }

                if (pts.size >= 2) {
                    // Catmull-Rom smooth path up to drawProgress fraction
                    fun buildPath(clamp: Float): Path =
                        Path().apply {
                            moveTo(pts[0].x, pts[0].y)
                            val cutIndex = ((n - 1) * clamp).toInt().coerceIn(0, n - 2)
                            for (i in 0..cutIndex) {
                                val p0 = if (i > 0) pts[i - 1] else pts[i]
                                val p1 = pts[i]
                                val p2 = pts[(i + 1).coerceAtMost(n - 1)]
                                val p3 =
                                    if (i < n - 2) pts[i + 2] else pts[(i + 1).coerceAtMost(n - 1)]
                                val segFrac =
                                    if (i == cutIndex) ((clamp * (n - 1)) - i).coerceIn(0f, 1f)
                                    else 1f
                                val tx = p1.x + (p2.x - p0.x) / 6f
                                val ty = p1.y + (p2.y - p0.y) / 6f
                                val ux = p2.x - (p3.x - p1.x) / 6f
                                val uy = p2.y - (p3.y - p1.y) / 6f
                                cubicTo(
                                    p1.x + (tx - p1.x) * segFrac,
                                    p1.y + (ty - p1.y) * segFrac,
                                    p1.x + (ux - p1.x) * segFrac,
                                    p1.y + (uy - p1.y) * segFrac,
                                    p1.x + (p2.x - p1.x) * segFrac,
                                    p1.y + (p2.y - p1.y) * segFrac,
                                )
                            }
                        }

                    val sp = buildPath(progress)

                    // fill under curve
                    val fillPath =
                        Path().apply {
                            addPath(sp)
                            val lastPtX = pts[((n - 1) * progress).toInt().coerceAtMost(n - 1)].x
                            lineTo(lastPtX, h - padB)
                            lineTo(pts[0].x, h - padB)
                            close()
                        }
                    drawPath(path = fillPath, color = lineColor.copy(alpha = 0.08f))

                    // stroke
                    drawPath(
                        path = sp,
                        color = lineColor,
                        style =
                            Stroke(
                                width = 2.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                    )

                    // Dots
                    val visibleCount = ((n - 1) * progress).toInt() + 1
                    pts.take(visibleCount).forEachIndexed { i, pt ->
                        val isSelected = i == selectedIndex
                        if (isSelected) {
                            // Scrubber line
                            drawLine(
                                color = lineColor.copy(alpha = 0.25f),
                                start = Offset(pt.x, padT),
                                end = Offset(pt.x, h - padB),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
                            )
                            drawCircle(
                                color = lineColor.copy(alpha = 0.15f),
                                center = pt,
                                radius = 8.dp.toPx(),
                            )
                            drawCircle(color = lineColor, center = pt, radius = 4.dp.toPx())
                            drawCircle(color = surfaceColor, center = pt, radius = 2.dp.toPx())
                        } else {
                            drawCircle(
                                color = lineColor.copy(alpha = 0.55f),
                                center = pt,
                                radius = 2.5.dp.toPx(),
                            )
                            drawCircle(color = surfaceColor, center = pt, radius = 1.2.dp.toPx())
                        }
                    }

                    // Value label at last point when not scrubbing
                    if (progress >= 1f && selectedIndex < 0) {
                        val lastPt = pts.last()
                        val label = "${visibleData.last()}"
                        val labelResult = textMeasurer.measure(label, style = labelStyle)
                        val lx =
                            if (lastPt.x + labelResult.size.width + 8.dp.toPx() <= w - padR)
                                lastPt.x + 8.dp.toPx()
                            else lastPt.x - labelResult.size.width - 8.dp.toPx()
                        drawText(
                            textLayoutResult = labelResult,
                            topLeft = Offset(lx, lastPt.y - labelResult.size.height / 2f),
                            color = lineColor,
                        )
                    }
                } else if (pts.size == 1) {
                    drawCircle(
                        color = lineColor.copy(alpha = 0.8f),
                        center = pts.first(),
                        radius = 4.dp.toPx(),
                    )
                    drawCircle(color = surfaceColor, center = pts.first(), radius = 2.dp.toPx())
                }
            }

            // X-axis range labels
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${visibleData.size}w ago",
                    style =
                        MaterialTheme.typography.labelSmall.copy(fontFamily = flexFontRounded()),
                    color = outlineVariantColor,
                )
                Text(
                    text = "now",
                    style =
                        MaterialTheme.typography.labelSmall.copy(fontFamily = flexFontRounded()),
                    color = outlineVariantColor,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else NotEnoughData()
    }
}
