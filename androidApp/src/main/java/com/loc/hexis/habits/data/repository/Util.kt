package com.loc.hexis.habits.data.repository

import com.loc.hexis.core.habits.Habit
import com.loc.hexis.core.habits.HabitStatus
import com.loc.hexis.core.habits.PointsSummary
import com.loc.hexis.core.habits.WeekDayFrequencyData
import com.loc.hexis.core.habits.WeeklyComparisonData
import com.loc.hexis.core.now
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

fun countCurrentStreak(
    dates: List<LocalDate>,
    eligibleWeekdays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
): Int = countStreakAtDate(dates, eligibleWeekdays, LocalDate.now())

fun countBestStreak(
    dates: List<LocalDate>,
    eligibleWeekdays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
): Int {
    if (dates.isEmpty()) return 0

    val filteredDates = dates.filter { eligibleWeekdays.contains(it.dayOfWeek) }.sorted()
    if (filteredDates.isEmpty()) return 0

    var maxConsecutive = 1
    var currentConsecutive = 1

    for (i in 1 until filteredDates.size) {
        val previousDate = filteredDates[i - 1]
        val currentDate = filteredDates[i]

        if (areConsecutiveEligibleDays(previousDate, currentDate, eligibleWeekdays)) {
            currentConsecutive++
        } else {
            maxConsecutive = maxOf(maxConsecutive, currentConsecutive)
            currentConsecutive = 1
        }
    }

    return maxOf(maxConsecutive, currentConsecutive)
}

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

internal fun areConsecutiveEligibleDays(
    date1: LocalDate,
    date2: LocalDate,
    eligibleWeekdays: Set<DayOfWeek>,
): Boolean {
    var checkDate = date1.plus(1, DateTimeUnit.DAY)
    while (checkDate < date2) {
        if (eligibleWeekdays.contains(checkDate.dayOfWeek)) {
            // Found an eligible day between date1 and date2, so they're not consecutive
            return false
        }
        checkDate = checkDate.plus(1, DateTimeUnit.DAY)
    }
    return checkDate == date2
}

fun countStreakAtDate(
    dates: List<LocalDate>,
    eligibleWeekdays: Set<DayOfWeek>,
    referenceDate: LocalDate,
): Int {
    val filtered = dates.filter { it <= referenceDate && it.dayOfWeek in eligibleWeekdays }.sorted()
    if (filtered.isEmpty()) return 0

    val last = filtered.last()
    val gap = last.daysUntil(referenceDate)
    if (gap > 0) {
        var missedEligible = false
        for (i in 1..gap) {
            val d = last.plus(DatePeriod(days = i))
            if (d.dayOfWeek in eligibleWeekdays && d <= referenceDate) {
                missedEligible = true
                break
            }
        }
        if (missedEligible) return 0
    }

    var streak = 1
    for (i in filtered.size - 2 downTo 0) {
        if (areConsecutiveEligibleDays(filtered[i], filtered[i + 1], eligibleWeekdays)) {
            streak++
        } else break
    }
    return streak
}

fun computePointsSummary(
    habit: Habit,
    completedStatuses: List<HabitStatus>,
    firstDay: DayOfWeek,
): PointsSummary {
    if (completedStatuses.isEmpty()) return PointsSummary()

    val eligible = habit.days
    val today = LocalDate.now()
    val totalWeeks = 52

    val weeklyPoints = mutableMapOf<LocalDate, Int>()
    var totalPoints = 0

    for (status in completedStatuses.sortedBy { it.date }) {
        val allDatesUpTo = completedStatuses.filter { it.date <= status.date }.map { it.date }
        val streak = countStreakAtDate(allDatesUpTo, eligible, status.date)
        val pts = 10 + (streak * 3)
        totalPoints += pts

        val daysFromFirst = (status.date.dayOfWeek.isoDayNumber - firstDay.isoDayNumber + 7) % 7
        val weekStart = status.date.minus(daysFromFirst, DateTimeUnit.DAY)
        weeklyPoints[weekStart] = (weeklyPoints[weekStart] ?: 0) + pts
    }

    val todayWeekStart =
        today.minus(
            (today.dayOfWeek.isoDayNumber - firstDay.isoDayNumber + 7) % 7,
            DateTimeUnit.DAY,
        )
    val periodStart = todayWeekStart.minus(totalWeeks, DateTimeUnit.WEEK)

    val history =
        (0..totalWeeks).map { i -> weeklyPoints[periodStart.plus(i, DateTimeUnit.WEEK)] ?: 0 }

    val currentWeekStart = todayWeekStart
    val lastWeekStart = currentWeekStart.minus(1, DateTimeUnit.WEEK)

    return PointsSummary(
        currentWeekPoints = weeklyPoints[currentWeekStart] ?: 0,
        lastWeekPoints = weeklyPoints[lastWeekStart] ?: 0,
        totalPoints = totalPoints,
        weeklyPointsHistory = history,
    )
}

fun filterCompletedStatuses(habit: Habit, statuses: List<HabitStatus>): List<HabitStatus> {
    return statuses.filter { it.value >= (habit.targetValue ?: 1.0) }
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
