package com.loc.hexis.shared.ui.habit.ui.component.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HeatMapCalendar
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapCalendarState
import com.kizitonwose.calendar.compose.heatmapcalendar.rememberHeatMapCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.now
import com.loc.hexis.core.habits.DisplayMode
import com.loc.hexis.core.habits.HabitStatus
import com.loc.hexis.core.habits.StreakPosition
import com.loc.hexis.core.habits.areConsecutiveEligibleDays
import com.loc.hexis.shared.ui.HexisPreviewWrapper
import com.loc.hexis.shared.ui.components.endItemShape
import com.loc.hexis.shared.ui.components.leadingItemShape
import com.loc.hexis.shared.ui.habit.daysStartingFrom
import com.loc.hexis.shared.ui.habit.ui.component.AnalyticsCard
import com.loc.hexis.shared.ui.habit.ui.component.CardArrows
import com.loc.hexis.shared.ui.heatMapStreakShape
import com.loc.hexis.shared.ui.theme.flexFontRounded
import com.loc.hexis.shared.ui.util.rememberToday
import hexis.shared.ui.generated.resources.*
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import org.jetbrains.compose.resources.stringResource

@Composable
fun WeeklyBooleanHeatMap(
    heatMapState: HeatMapCalendarState,
    days: Set<DayOfWeek>,
    startDate: LocalDate,
    statuses: List<HabitStatus>,
    targetValue: Double,
    displayMode: DisplayMode,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today by rememberToday()
    val scope = rememberCoroutineScope()

    val isProgress = displayMode == DisplayMode.PROGRESS && targetValue > 1.0
    val statusMap = remember(statuses) { statuses.associateBy { it.date } }

    val doneDates =
        remember(statuses, targetValue) {
            statuses.filter { it.value >= targetValue }.map { it.date }.toSet()
        }
    val edgeWeeks =
        listOf(heatMapState.firstDayOfWeek, daysStartingFrom(heatMapState.firstDayOfWeek).last())

    AnalyticsCard(
        title = stringResource(Res.string.weekly_progress),
        icon = Res.drawable.view_week,
        modifier = modifier,
        header = {
            CardArrows(
                onBackAction = { scope.launch { heatMapState.animateScrollBy(-150f) } },
                onForwardAction = { scope.launch { heatMapState.animateScrollBy(150f) } },
            )
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                val days = daysStartingFrom(heatMapState.firstDayOfWeek)

                days.forEachIndexed { index, dayOfWeek ->
                    val shape =
                        when (index) {
                            0 -> leadingItemShape(topRadius = 20)
                            days.size - 1 -> endItemShape(bottomRadius = 20)
                            else -> RoundedCornerShape(4.dp)
                        }
                    Box(
                        modifier =
                            Modifier.padding(start = 6.dp, top = 1.dp, bottom = 1.dp, end = 6.dp)
                                .size(32.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = shape,
                                )
                    ) {
                        Text(
                            text = dayOfWeek.name.take(1),
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }

            Row(
                modifier =
                    Modifier.padding(bottom = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = MaterialTheme.shapes.medium,
                        )
            ) {
                HeatMapCalendar(
                    state = heatMapState,
                    contentPadding = PaddingValues(8.dp),
                    monthHeader = {
                        Box(modifier = Modifier.padding(2.dp)) {
                            Text(
                                text =
                                    it.yearMonth.format(
                                        YearMonth.Format {
                                            monthName(MonthNames.ENGLISH_ABBREVIATED)
                                            char(' ')
                                            year()
                                        }
                                    ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                    },
                    dayContent = { day, _ ->
                        if (day.date > today) return@HeatMapCalendar

                        val validDay = day.date.dayOfWeek in days && day.date >= startDate

                        if (isProgress) {
                            val dayStatus = statusMap[day.date]
                            val value = dayStatus?.value ?: 0.0
                            val progress =
                                if (targetValue > 0.0)
                                    (value / targetValue).coerceIn(0.0, 1.0).toFloat()
                                else 0f
                            val completed = value >= targetValue

                            HeatMapProgressCell(
                                day = day,
                                progress = progress,
                                completed = completed,
                                validDay = validDay,
                                startDate = startDate,
                                onDateClick = onDateClick,
                            )
                            return@HeatMapCalendar
                        }

                        val done = day.date in doneDates

                        val hasPreviousEligibleCompleted =
                            doneDates.any { completedDate ->
                                completedDate < day.date &&
                                    completedDate.dayOfWeek in days &&
                                    areConsecutiveEligibleDays(completedDate, day.date, days)
                            }
                        val hasNextEligibleCompleted =
                            doneDates.any { completedDate ->
                                completedDate > day.date &&
                                    completedDate.dayOfWeek in days &&
                                    areConsecutiveEligibleDays(day.date, completedDate, days)
                            }
                        val streakPosition: StreakPosition =
                            when {
                                hasPreviousEligibleCompleted && hasNextEligibleCompleted -> MIDDLE
                                hasPreviousEligibleCompleted -> END
                                hasNextEligibleCompleted -> START
                                else -> ISOLATED
                            }

                        val shape =
                            heatMapStreakShape(
                                streakPosition = streakPosition,
                                isFirstDayOfWeek = day.date.dayOfWeek == edgeWeeks.first(),
                                isLastDayOfWeek = day.date.dayOfWeek == edgeWeeks.last(),
                            )

                        Box(
                            modifier =
                                Modifier.padding(horizontal = 1.dp)
                                    .size(35.dp)
                                    .clip(shape)
                                    .clickable(enabled = validDay) { onDateClick(day.date) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (done) {
                                Box(
                                    modifier =
                                        Modifier.fillMaxSize()
                                            .background(color = MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    val isStreakEnd =
                                        streakPosition == StreakPosition.START ||
                                            streakPosition == StreakPosition.END
                                    val innerMod =
                                        if (day.date == startDate && streakPosition != ISOLATED)
                                            Modifier.border(
                                                1.dp,
                                                Color(0xFFFFD700),
                                                MaterialShapes.Circle.toShape(),
                                            )
                                        else Modifier
                                    if (isStreakEnd) {
                                        Box(
                                            modifier =
                                                Modifier.size(30.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        shape = MaterialShapes.Circle.toShape(),
                                                    )
                                                    .then(innerMod),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = day.date.day.toString(),
                                                style =
                                                    MaterialTheme.typography.bodyMedium.copy(
                                                        fontFamily = flexFontRounded()
                                                    ),
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = day.date.day.toString(),
                                            style =
                                                MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = flexFontRounded()
                                                ),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = day.date.day.toString(),
                                    style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = flexFontRounded()
                                        ),
                                    color =
                                        if (!validDay)
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(4.dp),
                                )
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun HeatMapProgressCell(
    day: CalendarDay,
    progress: Float,
    completed: Boolean,
    validDay: Boolean,
    startDate: LocalDate,
    onDateClick: (LocalDate) -> Unit,
) {
    val cellSize = 30.dp
    val strokeWidth = 2.dp
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceLow = MaterialTheme.colorScheme.surfaceContainerLow
    val isStart = day.date == startDate
    val circleShape = MaterialShapes.Circle.toShape()

    val borderMod: (Modifier) -> Modifier = { m ->
        if (isStart && completed) m.border(width = 1.dp, color = Color(0xFFFFD700), shape = circleShape) else m
    }

    Box(
        modifier =
            Modifier.padding(horizontal = 1.dp).size(35.dp).clickable(enabled = validDay) {
                onDateClick(day.date)
            },
        contentAlignment = Alignment.Center,
    ) {
        if (completed) {
            Box(
                modifier =
                    borderMod(
                        Modifier.size(cellSize)
                            .background(color = primary, shape = MaterialShapes.Circle.toShape())
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day.date.day.toString(),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = flexFontRounded(),
                            color = onPrimary,
                        ),
                )
            }
        } else if (progress > 0f) {
            Box(
                modifier =
                    borderMod(
                        Modifier.size(cellSize)
                            .background(color = surfaceLow, shape = MaterialShapes.Circle.toShape())
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sw = strokeWidth.toPx()
                    val arcS = Size(this.size.width - sw, this.size.height - sw)
                    drawArc(
                        color = primary,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        topLeft = Offset(sw / 2f, sw / 2f),
                        size = arcS,
                        style = Stroke(width = sw, cap = StrokeCap.Round),
                    )
                }
                Text(
                    text = day.date.day.toString(),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = flexFontRounded(),
                            color = onSurface,
                        ),
                )
            }
        } else {
            Text(
                text = day.date.day.toString(),
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = flexFontRounded(),
                        color = if (!validDay) onSurface.copy(alpha = 0.5f) else onSurface,
                    ),
                modifier = Modifier.padding(4.dp),
            )
        }
    }
}

@PreviewWrapper(HexisPreviewWrapper::class)
@Preview
@Composable
private fun Preview() {
    WeeklyBooleanHeatMap(
        heatMapState =
            rememberHeatMapCalendarState(
                startMonth = YearMonth.now().minus(1, DateTimeUnit.YEAR),
                endMonth = YearMonth.now(),
                firstVisibleMonth = YearMonth.now(),
                firstDayOfWeek = DayOfWeek.MONDAY,
            ),
        statuses =
            (0..40).map {
                HabitStatus(habitId = 1, date = LocalDate.now().minus(it, DateTimeUnit.DAY))
            },
        targetValue = 1.0,
        displayMode = DisplayMode.CHECKBOX,
        days = DayOfWeek.entries.toSet(),
        startDate = LocalDate.now().minus(40, DateTimeUnit.DAY),
        onDateClick = {},
    )
}
