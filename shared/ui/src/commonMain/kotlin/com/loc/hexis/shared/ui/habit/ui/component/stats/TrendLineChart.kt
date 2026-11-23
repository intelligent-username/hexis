
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.loc.hexis.core.toFormattedString
import com.loc.hexis.shared.ui.habit.ui.component.AnalyticsCard
import com.loc.hexis.shared.ui.habit.ui.component.NotEnoughData
import com.loc.hexis.shared.ui.theme.flexFontRounded
import com.loc.hexis.shared.ui.util.rememberToday
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.chart_data
import hexis.shared.ui.generated.resources.chart_point_tooltip
import hexis.shared.ui.generated.resources.points_trend
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

private data class NiceScale(val chartMax: Int, val step: Int, val gridCount: Int)

private data class TrendPoint(val date: LocalDate, val points: Int)

private fun calculateNiceScale(rawMax: Int): NiceScale {
    if (rawMax <= 2) return NiceScale(chartMax = 2, step = 1, gridCount = 2)
    if (rawMax <= 4) return NiceScale(chartMax = rawMax, step = 1, gridCount = rawMax)

    val roughStep = rawMax.toDouble() / 4
    val exponent = floor(log10(roughStep))
    val powerOfTen = 10.0.pow(exponent)
    val candidates =
        if (exponent >= 1.0) listOf(1.0, 2.0, 2.5, 5.0, 10.0)
        else listOf(1.0, 2.0, 5.0, 10.0)

    var bestStep = (candidates.first() * powerOfTen).roundToInt().coerceAtLeast(1)
    var bestDiff = Int.MAX_VALUE
    var bestChartMax = rawMax

    for (c in candidates) {
        val s = (c * powerOfTen).roundToInt().coerceAtLeast(1)
        val max = ceil(rawMax.toDouble() / s).toInt() * s
        val intervals = max / s
        if (intervals in 2..6) {
            val diff = abs(intervals - 4)
            if (diff < bestDiff || (diff == bestDiff && max < bestChartMax)) {
                bestDiff = diff
                bestStep = s
                bestChartMax = max
            }
        }
    }

    return NiceScale(bestChartMax, bestStep, (bestChartMax / bestStep).coerceAtLeast(1))
}

@Composable
fun TrendLineChart(
    weeklyPointsHistory: List<Int>,
    dailyPointsHistory: List<Int> = emptyList(),
    modifier: Modifier = Modifier,
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
) {
    var selectedTimePeriod by rememberSaveable { mutableStateOf(WeeklyTimePeriod.DAYS_7) }
    val historyToday by rememberToday()
    val isDaily = selectedTimePeriod == WeeklyTimePeriod.DAYS_7
    val history = if (isDaily) dailyPointsHistory else weeklyPointsHistory
    val slots = if (isDaily) 7 else selectedTimePeriod.toWeeks()
    val weekStart =
        historyToday.minus(
            (historyToday.dayOfWeek.isoDayNumber - firstDayOfWeek.isoDayNumber + 7) % 7,
            DateTimeUnit.DAY,
        )

    fun pagePoints(page: Int): List<TrendPoint> {
        val endExclusive = (history.size - page * slots).coerceAtLeast(0)
        val start = (endExclusive - slots).coerceAtLeast(0)
        return (start until endExclusive).map { index ->
            val offset = history.lastIndex - index
            val date =
                if (isDaily) historyToday.minus(offset, DateTimeUnit.DAY)
                else weekStart.minus(offset, DateTimeUnit.WEEK)
            TrendPoint(date, history[index])
        }
    }

    val pageCount = ((history.size + slots - 1) / slots).coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { pageCount })

    LaunchedEffect(selectedTimePeriod) { pagerState.scrollToPage(0) }
    LaunchedEffect(pageCount) {
        if (pagerState.currentPage >= pageCount) pagerState.scrollToPage(pageCount - 1)
    }

    val pageDataList = pagePoints(pagerState.currentPage)

    val hasAnyData = dailyPointsHistory.isNotEmpty() || weeklyPointsHistory.isNotEmpty()

    // Draw-in animation: line traces itself left to right (initial view / period change only)
    val drawProgress = remember(selectedTimePeriod) { Animatable(0f) }
    LaunchedEffect(selectedTimePeriod) {
        drawProgress.snapTo(0f)
        drawProgress.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
    }

    val selectedIndexState =
        remember(
            pagerState.currentPage,
            selectedTimePeriod,
            pageDataList,
            historyToday,
            firstDayOfWeek,
            pagerState.isScrollInProgress,
        ) {
            mutableIntStateOf(-1)
        }
    var selectedIndex by selectedIndexState

    AnalyticsCard(
        title = stringResource(Res.string.points_trend),
        icon = Res.drawable.chart_data,
        modifier = modifier.heightIn(max = 420.dp),
    ) {
        if (hasAnyData) {
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
                            selectedIndex = -1
                        },
                        shapes =
                            when (period) {
                                WeeklyTimePeriod.DAYS_7 ->
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

            if (history.isEmpty()) {
                NotEnoughData()
                return@AnalyticsCard
            }

            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(28.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                val tooltipVal =
                    if (!pagerState.isScrollInProgress) pageDataList.getOrNull(selectedIndex)
                    else null
                if (tooltipVal != null) {
                    val day = tooltipVal.date.day
                    val suffix =
                        if (day % 100 in 11..13) "th"
                        else when (day % 10) {
                            1 -> "st"
                            2 -> "nd"
                            3 -> "rd"
                            else -> "th"
                        }
                    val dateLabel =
                        tooltipVal.date.format(
                            LocalDate.Format {
                                monthName(MonthNames.ENGLISH_FULL)
                                chars(" $day$suffix ")
                                year()
                            }
                        )
                    val labelText =
                        stringResource(Res.string.chart_point_tooltip, tooltipVal.points, dateLabel)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 0.dp,
                    ) {
                        Text(
                            text = labelText,
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
            val textMeasurer = rememberTextMeasurer()
            val labelStyle =
                MaterialTheme.typography.labelSmall.copy(fontFamily = flexFontRounded())
            val progress = drawProgress.value

            val niceScale =
                remember(selectedTimePeriod, weeklyPointsHistory, dailyPointsHistory) {
                    val fullList =
                        if (selectedTimePeriod == WeeklyTimePeriod.DAYS_7) dailyPointsHistory
                        else weeklyPointsHistory
                    val rawMax = fullList.maxOrNull()?.coerceAtLeast(1) ?: 1
                    calculateNiceScale(rawMax)
                }

            HorizontalPager(
                state = pagerState,
                reverseLayout = true,
                modifier = Modifier.fillMaxWidth().height(210.dp),
            ) { page ->
                val currentPoints = pagePoints(page)
                val currentData = currentPoints.map { it.points }
                Canvas(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(210.dp)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .pointerInput(page, selectedTimePeriod, currentPoints, selectedIndexState) {
                                val padL = 36.dp.toPx()
                                val padR = 12.dp.toPx()
                                val gW = (size.width - padL - padR).coerceAtLeast(1f)
                                val n = currentData.size
                                detectTapGestures { offset ->
                                    if (page == pagerState.currentPage && !pagerState.isScrollInProgress) {
                                        selectedIndex =
                                            if (n == 1) 0
                                            else if (n > 1) {
                                                val frac = ((offset.x - padL) / gW).coerceIn(0f, 1f)
                                                (frac * (n - 1)).roundToInt().coerceIn(0, n - 1)
                                            } else -1
                                    }
                                }
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
                    val n = currentData.size

                    fun xOf(i: Int) = padL + (i.toFloat() / (n - 1).coerceAtLeast(1)) * gW
                    fun yOf(v: Int) = h - padB - (v.toFloat() / niceScale.chartMax) * gH

                    val pts = currentData.mapIndexed { i, v -> Offset(xOf(i), yOf(v)) }

                    // Dashed grid lines
                    val gridCount = niceScale.gridCount
                    for (i in 0..gridCount) {
                        val y = padT + (gH / gridCount) * i
                        drawLine(
                            color = outlineVariantColor.copy(alpha = 0.3f),
                            start = Offset(padL, y),
                            end = Offset(w - padR, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)),
                        )
                        val labelVal = niceScale.chartMax - niceScale.step * i
                        val labelResult = textMeasurer.measure("$labelVal", style = labelStyle)
                        drawText(
                            textLayoutResult = labelResult,
                            topLeft = Offset(0f, y - labelResult.size.height / 2f),
                            color = outlineVariantColor,
                        )
                    }

                    if (pts.size >= 2) {
                        fun buildPath(clamp: Float): Path =
                            Path().apply {
                                moveTo(pts[0].x, pts[0].y)
                                val cutIndex = ((n - 1) * clamp).toInt().coerceIn(0, n - 2)
                                for (i in 0..cutIndex) {
                                    val p0 = if (i > 0) pts[i - 1] else pts[i]
                                    val p1 = pts[i]
                                    val p2 = pts[(i + 1).coerceAtMost(n - 1)]
                                    val p3 =
                                        if (i < n - 2) pts[i + 2]
                                        else pts[(i + 1).coerceAtMost(n - 1)]
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
                                val lastPtX =
                                    pts[((n - 1) * progress).toInt().coerceAtMost(n - 1)].x
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
                            val isSelected =
                                page == pagerState.currentPage &&
                                    !pagerState.isScrollInProgress &&
                                    i == selectedIndex
                            if (isSelected) {
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
                                drawCircle(
                                    color = surfaceColor,
                                    center = pt,
                                    radius = 1.2.dp.toPx(),
                                )
                            }
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
            }

            // X-axis range labels
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val rangeEnd =
                    pageDataList.lastOrNull()?.date?.let { date ->
                        if (isDaily) date
                        else minOf(historyToday, date.plus(6, DateTimeUnit.DAY))
                    }
                Text(
                    text = pageDataList.firstOrNull()?.date?.toFormattedString().orEmpty(),
                    style =
                        MaterialTheme.typography.labelSmall.copy(fontFamily = flexFontRounded()),
                    color = outlineVariantColor,
                )
                Text(
                    text = rangeEnd?.toFormattedString().orEmpty(),
                    style =
                        MaterialTheme.typography.labelSmall.copy(fontFamily = flexFontRounded()),
                    color = outlineVariantColor,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else NotEnoughData()
    }
}
