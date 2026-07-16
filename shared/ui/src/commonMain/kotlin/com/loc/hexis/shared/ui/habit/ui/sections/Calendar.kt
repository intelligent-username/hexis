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

package com.loc.hexis.shared.ui.habit.ui.sections

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.VerticalYearCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.yearcalendar.rememberYearCalendarState
import com.kizitonwose.calendar.core.Year
import com.loc.hexis.core.habits.CalendarType
import com.loc.hexis.core.habits.DisplayMode
import com.loc.hexis.core.habits.Habit
import com.loc.hexis.core.habits.HabitWithAnalytics
import com.loc.hexis.shared.ui.LocalWindowSizeClass
import com.loc.hexis.shared.ui.habit.HabitState
import com.loc.hexis.shared.ui.habit.daysStartingFrom
import com.loc.hexis.shared.ui.habit.ui.component.CalendarMonthHeader
import com.loc.hexis.shared.ui.habit.ui.component.MonthlyCalendarDayContent
import com.loc.hexis.shared.ui.habit.ui.component.YearlyCalendarDayContent
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.theme.flexFontRounded
import com.loc.hexis.shared.ui.toStringRes
import com.loc.hexis.shared.ui.util.rememberToday
import hexis.shared.ui.generated.resources.*
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun Calendar(
    state: HabitState,
    onDateClick: (Habit, LocalDate) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var calendarType by rememberSaveable { mutableStateOf(CalendarType.MONTH) }

    val currentHabit =
        state.habitsWithAnalytics.find { it.habit.id == state.analyticsHabitId } ?: return
    val windowSizeClass = LocalWindowSizeClass.current
    val today by rememberToday()
    val targetValue = currentHabit.habit.targetValue ?: 1.0
    val displayMode = currentHabit.habit.displayMode
    val edgeWeeks = listOf(state.startingDay, daysStartingFrom(state.startingDay).last())

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(text = stringResource(Res.string.calendar), fontFamily = flexFontEmphasis())
            },
            navigationIcon = {
                FilledTonalIconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.nav_arrow_back),
                        contentDescription = "Navigate Back",
                    )
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = Color.Transparent,
                    containerColor = Color.Transparent,
                ),
            windowInsets =
                if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
                    WindowInsets(0)
                } else {
                    TopAppBarDefaults.windowInsets
                },
        )

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            CalendarType.entries.forEach { entry ->
                ToggleButton(
                    checked = entry == calendarType,
                    onCheckedChange = { calendarType = entry },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(entry.toStringRes()))
                }
            }
        }

        AnimatedContent(targetState = calendarType, modifier = Modifier.fillMaxSize()) { type ->
            when (type) {
                YEAR -> {
                    YearlyCalendar(
                        today = today,
                        state = state,
                        modifier = modifier,
                        currentHabit = currentHabit,
                        targetValue = targetValue,
                        displayMode = displayMode,
                        edgeWeeks = edgeWeeks,
                        onDateClick = onDateClick,
                    )
                }

                MONTH -> {
                    MonthlyCalendar(
                        state = state,
                        today = today,
                        currentHabit = currentHabit,
                        targetValue = targetValue,
                        displayMode = displayMode,
                        edgeWeeks = edgeWeeks,
                        onDateClick = onDateClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun YearlyCalendar(
    today: LocalDate,
    state: HabitState,
    modifier: Modifier,
    currentHabit: HabitWithAnalytics,
    targetValue: Double,
    displayMode: DisplayMode,
    onDateClick: (Habit, LocalDate) -> Unit,
    edgeWeeks: List<DayOfWeek>,
) {
    val calendarState =
        rememberYearCalendarState(
            startYear = Year(2024),
            endYear = Year(today.yearMonth.year),
            firstVisibleYear = Year(today.yearMonth.year),
            firstDayOfWeek = state.startingDay,
        )

    VerticalYearCalendar(
        modifier = modifier.padding(horizontal = 8.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 60.dp),
        monthHorizontalSpacing = 4.dp,
        monthVerticalSpacing = 4.dp,
        state = calendarState,
        reverseLayout = true,
        calendarScrollPaged = false,
        yearHeader = { calendarYear ->
            Box(modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 48.dp, bottom = 0.dp)) {
                Text(
                    text = calendarYear.year.value.toString(),
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontFamily = flexFontRounded(),
                        ),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        },
        monthHeader = { calendarMonth ->
            if (calendarMonth.yearMonth <= today.yearMonth) {
                CalendarMonthHeader(
                    calendarMonth = calendarMonth,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        },
        dayContent = { day ->
            if (day.date.yearMonth <= today.yearMonth) {
                YearlyCalendarDayContent(
                    day = day,
                    statuses = currentHabit.statuses,
                    targetValue = targetValue,
                    displayMode = displayMode,
                    today = today,
                    habitDays = currentHabit.habit.days,
                    startDate = currentHabit.habit.time.date,
                    edgeWeeks = edgeWeeks,
                    onDateClick = { onDateClick(currentHabit.habit, it) },
                )
            }
        },
    )
}

@Composable
private fun MonthlyCalendar(
    state: HabitState,
    today: LocalDate,
    currentHabit: HabitWithAnalytics,
    targetValue: Double,
    displayMode: DisplayMode,
    edgeWeeks: List<DayOfWeek>,
    onDateClick: (Habit, LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val calendarState =
        rememberCalendarState(
            startMonth = YearMonth(year = 2024, month = Month.JANUARY),
            endMonth = today.yearMonth,
            firstVisibleMonth = today.yearMonth,
            firstDayOfWeek = state.startingDay,
        )

    VerticalCalendar(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 60.dp),
        state = calendarState,
        reverseLayout = true,
        monthHeader = { calendarMonth -> CalendarMonthHeader(calendarMonth = calendarMonth) },
        dayContent = { day ->
            MonthlyCalendarDayContent(
                day = day,
                statuses = currentHabit.statuses,
                targetValue = targetValue,
                displayMode = displayMode,
                today = today,
                habitDays = currentHabit.habit.days,
                startDate = currentHabit.habit.time.date,
                edgeWeeks = edgeWeeks,
                onDateClick = { onDateClick(currentHabit.habit, it) },
            )
        },
    )
}
