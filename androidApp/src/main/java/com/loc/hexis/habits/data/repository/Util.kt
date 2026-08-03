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

package com.loc.hexis.habits.data.repository

import com.loc.hexis.core.habits.DisplayMode
import com.loc.hexis.core.habits.Habit
import com.loc.hexis.core.habits.HabitStatus
import com.loc.hexis.core.habits.PointsSummary
import com.loc.hexis.core.habits.WeekDayFrequencyData
import com.loc.hexis.core.habits.WeeklyComparisonData
import com.loc.hexis.core.habits.countStreakAtDate
import com.loc.hexis.core.now
import kotlin.math.roundToInt
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

fun prepareLineChartData(
    firstDay: DayOfWeek,
    habitStatuses: List<HabitStatus>,
    targetValue: Double,
): WeeklyComparisonData {
    val today = LocalDate.now()
    val totalWeeks = 52

    val startDateOfTodayWeek =
        today.minus(
            (today.dayOfWeek.isoDayNumber - firstDay.isoDayNumber + 7) % 7,
            DateTimeUnit.DAY,
        )
    val startDateOfPeriod = startDateOfTodayWeek.minus(totalWeeks, DateTimeUnit.WEEK)

    val habitCompletionByWeek =
        habitStatuses
            .filter { it.date in startDateOfPeriod..today }
            .groupBy {
                val daysFromFirstDay =
                    (it.date.dayOfWeek.isoDayNumber - firstDay.isoDayNumber + 7) % 7
                it.date.minus(daysFromFirstDay, DateTimeUnit.DAY)
            }
            .mapValues { (_, statuses) ->
                statuses.sumOf { minOf(it.value, targetValue) / targetValue }
            }

    val values =
        (0..totalWeeks).map { i ->
            val currentWeekStart = startDateOfPeriod.plus(i, DateTimeUnit.WEEK)
            val weekKey = currentWeekStart
            (habitCompletionByWeek[weekKey] ?: 0.0).coerceIn(0.0, 7.0)
        }
    return values
}

fun prepareWeekDayFrequencyData(
    dates: List<LocalDate>,
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
): WeekDayFrequencyData {
    val dayFrequency = dates.groupingBy { it.dayOfWeek }.eachCount()

    val orderedDays = DayOfWeek.entries.toMutableList()
    val index = orderedDays.indexOf(firstDayOfWeek)
    val rotatedDays = orderedDays.subList(index, orderedDays.size) + orderedDays.subList(0, index)

    return rotatedDays.associate { dayOfWeek ->
        val weekName = DayOfWeekNames.ENGLISH_ABBREVIATED.names[dayOfWeek.isoDayNumber - 1]

        weekName to (dayFrequency[dayOfWeek] ?: 0)
    }
}

fun prepareHeatMapData(habitData: List<HabitStatus>): Map<LocalDate, Int> {
    val allDates = habitData.map { it.date }
    val dateFrequency = allDates.groupingBy { it }.eachCount()

    return dateFrequency
}

fun computePointsSummary(
    habit: Habit,
    allStatuses: List<HabitStatus>,
    firstDay: DayOfWeek,
): PointsSummary {
    if (allStatuses.isEmpty()) return PointsSummary()

    val eligible = habit.days
    val today = LocalDate.now()
    val totalWeeks = 52
    val isCounter = habit.displayMode == DisplayMode.PROGRESS
    val targetVal = habit.targetValue ?: 1.0

    val weeklyPoints = mutableMapOf<LocalDate, Int>()
    val dailyPoints = mutableMapOf<LocalDate, Int>()
    var totalPoints = 0

    val completedStatusesSoFar = mutableListOf<HabitStatus>()

    for (status in allStatuses.sortedBy { it.date }) {
        val isCompleted = status.value >= targetVal - 0.001
        val pts: Int =
            if (isCompleted) {
                completedStatusesSoFar.add(status)
                val streak =
                    countStreakAtDate(completedStatusesSoFar.map { it.date }, eligible, status.date)
                val basePts = 10 + (streak * 2)
                if (isCounter) (basePts * 1.10).roundToInt() else basePts
            } else if (isCounter && status.value > 0.0 && targetVal > 0.0) {
                val fraction = (status.value / targetVal).coerceIn(0.0, 1.0)
                val baseDayPts = 10.0 * 1.10
                (0.5 * fraction * baseDayPts).roundToInt()
            } else {
                0
            }

        if (pts > 0) {
            totalPoints += pts
            val daysFromFirst = (status.date.dayOfWeek.isoDayNumber - firstDay.isoDayNumber + 7) % 7
            val weekStart = status.date.minus(daysFromFirst, DateTimeUnit.DAY)
            weeklyPoints[weekStart] = (weeklyPoints[weekStart] ?: 0) + pts
            dailyPoints[status.date] = (dailyPoints[status.date] ?: 0) + pts
        }
    }

    val todayWeekStart =
        today.minus(
            (today.dayOfWeek.isoDayNumber - firstDay.isoDayNumber + 7) % 7,
            DateTimeUnit.DAY,
        )
    val periodStart = todayWeekStart.minus(totalWeeks, DateTimeUnit.WEEK)

    val history =
        (0..totalWeeks).map { i -> weeklyPoints[periodStart.plus(i, DateTimeUnit.WEEK)] ?: 0 }
    val dailyHistory =
        (0..6).map { i -> dailyPoints[today.minus(6 - i, DateTimeUnit.DAY)] ?: 0 }

    val currentWeekStart = todayWeekStart
    val lastWeekStart = currentWeekStart.minus(1, DateTimeUnit.WEEK)

    return PointsSummary(
        currentWeekPoints = weeklyPoints[currentWeekStart] ?: 0,
        lastWeekPoints = weeklyPoints[lastWeekStart] ?: 0,
        totalPoints = totalPoints,
        weeklyPointsHistory = history,
        dailyPointsHistory = dailyHistory,
    )
}

fun filterCompletedStatuses(habit: Habit, statuses: List<HabitStatus>): List<HabitStatus> {
    val target = (habit.targetValue ?: 1.0) - 0.001
    return statuses.filter { it.value >= target }
}

fun calculateConsistency(dates: List<LocalDate>, eligibleWeekdays: Set<DayOfWeek>): Float {
    val eligibleDates = dates.filter { it.dayOfWeek in eligibleWeekdays }
    val firstCompletionDate = eligibleDates.minOrNull() ?: return 0f
    val today = LocalDate.now()

    var totalEligibleDays = 0
    var current = firstCompletionDate
    while (current <= today) {
        if (current.dayOfWeek in eligibleWeekdays) {
            totalEligibleDays++
        }
        current = current.plus(1, DateTimeUnit.DAY)
    }

    return if (totalEligibleDays > 0) eligibleDates.size.toFloat() / totalEligibleDays else 0f
}
