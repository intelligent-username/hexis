package com.loc.hexis.shared.ui.habit.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.plusDays
import com.loc.hexis.core.habits.DisplayMode
import com.loc.hexis.core.habits.HabitStatus
import com.loc.hexis.core.habits.StreakPosition
import com.loc.hexis.core.habits.areConsecutiveEligibleDays
import com.loc.hexis.shared.ui.calendarMapStreakShape
import com.loc.hexis.shared.ui.theme.flexFontRounded
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char

@Composable
fun CalendarMonthHeader(
    calendarMonth: CalendarMonth,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    padding: PaddingValues = PaddingValues(start = 4.dp, end = 4.dp, top = 32.dp, bottom = 8.dp),
) {
    Box(modifier = modifier.padding(padding)) {
        Text(
            text =
                calendarMonth.yearMonth.format(
                    YearMonth.Format {
                        monthName(MonthNames.ENGLISH_FULL)
                        char(' ')
                        year()
                    }
                ),
            style =
                style.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = flexFontRounded(),
                ),
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
fun YearlyCalendarDayContent(
    day: CalendarDay,
    statuses: List<HabitStatus>,
    targetValue: Double,
    displayMode: DisplayMode,
    today: LocalDate,
    habitDays: Set<DayOfWeek>,
    startDate: LocalDate,
    edgeWeeks: List<DayOfWeek>,
    modifier: Modifier = Modifier,
    style: TextStyle =
        MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontFamily = flexFontRounded()),
    onDateClick: (LocalDate) -> Unit,
) {
    if (day.position != DayPosition.MonthDate) return

    val isProgress = displayMode == DisplayMode.PROGRESS && targetValue > 1.0

    val statusMap = remember(statuses) { statuses.associateBy { it.date } }
    val dayStatus = statusMap[day.date]
    val value = dayStatus?.value ?: 0.0
    val progress = if (targetValue > 0.0) (value / targetValue).coerceIn(0.0, 1.0) else 0.0
    val isCompleted = value >= targetValue
    val validDate = day.date <= today && day.date >= startDate && day.date.dayOfWeek in habitDays

    if (isProgress) {
        ProgressDayCell(
            day = day,
            progress = progress.toFloat(),
            isCompleted = isCompleted,
            validDate = validDate,
            startDate = startDate,
            onDateClick = onDateClick,
            modifier = modifier,
            height = 20.dp,
            style = style,
        )
        return
    }

    val doneDates =
        remember(statuses, targetValue) {
            statuses.filter { it.value >= targetValue }.map { it.date }.toSet()
        }
    val done = day.date in doneDates

    val hasPreviousEligibleCompleted =
        doneDates.any { completedDate ->
            completedDate < day.date &&
                completedDate.dayOfWeek in habitDays &&
                areConsecutiveEligibleDays(completedDate, day.date, habitDays)
        }
    val hasNextEligibleCompleted =
        doneDates.any { completedDate ->
            completedDate > day.date &&
                completedDate.dayOfWeek in habitDays &&
                areConsecutiveEligibleDays(day.date, completedDate, habitDays)
        }
    val streakPosition: StreakPosition =
        when {
            hasPreviousEligibleCompleted && hasNextEligibleCompleted -> MIDDLE
            hasPreviousEligibleCompleted -> END
            hasNextEligibleCompleted -> START
            else -> ISOLATED
        }

    Box(
        modifier =
            modifier
                .padding(top = 1.dp, bottom = 1.dp)
                .fillMaxWidth()
                .height(20.dp)
                .clip(
                    calendarMapStreakShape(
                        streakPosition = streakPosition,
                        isFirstDayOfWeek = day.date.dayOfWeek == edgeWeeks.first(),
                        isLastDayOfWeek = day.date.dayOfWeek == edgeWeeks.last(),
                        isFirstDayOfMonth = day.date.day == 1,
                        isLastDayOfMonth = day.date.plusDays(1).day == 1,
                    )
                )
                .clickable(enabled = validDate) { onDateClick(day.date) },
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            Box(
                modifier =
                    Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                val isStreakEnd = streakPosition == START || streakPosition == END

                if (isStreakEnd) {
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                                .padding(1.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    shape = CircleShape,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = day.date.day.toString(),
                            style = style,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else {
                    Text(
                        text = day.date.day.toString(),
                        style = style,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        } else if (validDate) {
            Box(
                modifier =
                    Modifier.matchParentSize()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = CircleShape,
                        )
            )
        }

        if (day.date == startDate && done && streakPosition != ISOLATED) {
            Box(
                modifier =
                    Modifier.matchParentSize()
                        .border(width = 1.dp, color = Color(0xFFFFD700), shape = CircleShape)
            )
        }
    }
}

@Composable
fun MonthlyCalendarDayContent(
    day: CalendarDay,
    statuses: List<HabitStatus>,
    targetValue: Double,
    displayMode: DisplayMode,
    today: LocalDate,
    habitDays: Set<DayOfWeek>,
    startDate: LocalDate,
    edgeWeeks: List<DayOfWeek>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    style: TextStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = flexFontRounded()),
) {
    if (day.position != DayPosition.MonthDate) return

    val isProgress = displayMode == DisplayMode.PROGRESS && targetValue > 1.0

    val statusMap = remember(statuses) { statuses.associateBy { it.date } }
    val dayStatus = statusMap[day.date]
    val value = dayStatus?.value ?: 0.0
    val progress = if (targetValue > 0.0) (value / targetValue).coerceIn(0.0, 1.0) else 0.0
    val isCompleted = value >= targetValue
    val validDate = day.date <= today && day.date >= startDate && day.date.dayOfWeek in habitDays

    if (isProgress) {
        ProgressDayCell(
            day = day,
            progress = progress.toFloat(),
            isCompleted = isCompleted,
            validDate = validDate,
            startDate = startDate,
            onDateClick = onDateClick,
            modifier = modifier,
            height = height,
            style = style,
        )
        return
    }

    val doneDates =
        remember(statuses, targetValue) {
            statuses.filter { it.value >= targetValue }.map { it.date }.toSet()
        }
    val done = day.date in doneDates

    val hasPreviousEligibleCompleted =
        doneDates.any { completedDate ->
            completedDate < day.date &&
                completedDate.dayOfWeek in habitDays &&
                areConsecutiveEligibleDays(completedDate, day.date, habitDays)
        }
    val hasNextEligibleCompleted =
        doneDates.any { completedDate ->
            completedDate > day.date &&
                completedDate.dayOfWeek in habitDays &&
                areConsecutiveEligibleDays(day.date, completedDate, habitDays)
        }
    val streakPosition: StreakPosition =
        when {
            hasPreviousEligibleCompleted && hasNextEligibleCompleted -> MIDDLE
            hasPreviousEligibleCompleted -> END
            hasNextEligibleCompleted -> START
            else -> ISOLATED
        }

    Box(
        modifier =
            modifier
                .padding(
                    top = 1.dp,
                    bottom = 1.dp,
                    start = if (day.date.dayOfWeek == edgeWeeks.first()) 4.dp else 0.dp,
                    end = if (day.date.dayOfWeek == edgeWeeks.last()) 4.dp else 0.dp,
                )
                .fillMaxWidth()
                .height(height)
                .clip(
                    calendarMapStreakShape(
                        streakPosition = streakPosition,
                        isFirstDayOfWeek = day.date.dayOfWeek == edgeWeeks.first(),
                        isLastDayOfWeek = day.date.dayOfWeek == edgeWeeks.last(),
                        isFirstDayOfMonth = day.date.day == 1,
                        isLastDayOfMonth = day.date.plusDays(1).day == 1,
                    )
                )
                .clickable(enabled = validDate) { onDateClick(day.date) },
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            Box(
                modifier =
                    Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                val isStreakEnd = streakPosition == START || streakPosition == END

                if (isStreakEnd) {
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                                .padding(1.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    shape = CircleShape,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = day.date.day.toString(),
                            style = style,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else {
                    Text(
                        text = day.date.day.toString(),
                        style = style,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        } else {
            Text(
                text = day.date.day.toString(),
                style = style,
                color =
                    if (!validDate) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurface,
            )
        }

        if (day.date == startDate && done && streakPosition != ISOLATED) {
            Box(
                modifier =
                    Modifier.matchParentSize()
                        .border(width = 1.dp, color = Color(0xFFFFD700), shape = CircleShape)
            )
        }
    }
}

@Composable
private fun ProgressDayCell(
    day: CalendarDay,
    progress: Float,
    isCompleted: Boolean,
    validDate: Boolean,
    startDate: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp,
    style: TextStyle,
) {
    val strokeWidth = 2.dp
    val isStart = day.date == startDate

    val borderMod: (Modifier) -> Modifier = { m ->
        if (isStart && isCompleted) m.border(width = 1.dp, color = Color(0xFFFFD700), shape = CircleShape) else m
    }

    Box(
        modifier =
            modifier.fillMaxWidth().height(height).clickable(enabled = validDate) {
                onDateClick(day.date)
            },
        contentAlignment = Alignment.Center,
    ) {
        val arcSize = height * 0.8f

        if (isCompleted) {
            Box(
                modifier =
                    borderMod(
                        Modifier.size(arcSize)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day.date.day.toString(),
                    style = style.copy(color = MaterialTheme.colorScheme.onPrimary),
                )
            }
        } else if (progress > 0f) {
            val arcColor = MaterialTheme.colorScheme.primary
            val bgColor = MaterialTheme.colorScheme.surfaceContainerLow

            Box(
                modifier =
                    borderMod(
                        Modifier.size(arcSize).background(color = bgColor, shape = CircleShape)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sw = strokeWidth.toPx()
                    drawArc(
                        color = arcColor,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        topLeft = Offset(sw / 2f, sw / 2f),
                        size = Size(size.width - sw, size.height - sw),
                        style = Stroke(width = sw, cap = StrokeCap.Round),
                    )
                }
                Text(
                    text = day.date.day.toString(),
                    style = style.copy(color = MaterialTheme.colorScheme.onSurface),
                )
            }
        } else if (validDate) {
            Box(
                modifier =
                    borderMod(
                        Modifier.size(arcSize)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = CircleShape,
                            )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day.date.day.toString(),
                    style =
                        style.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                )
            }
        } else {
            val textMod =
                if (isStart)
                    Modifier.border(width = 1.dp, color = Color(0xFFFFD700), shape = CircleShape)
                        .padding(4.dp)
                else Modifier.padding(4.dp)
            Text(
                text = day.date.day.toString(),
                style = style.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                modifier = textMod,
            )
        }
    }
}
