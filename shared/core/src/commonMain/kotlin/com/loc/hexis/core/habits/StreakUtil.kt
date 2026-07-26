package com.loc.hexis.core.habits

import com.loc.hexis.core.now
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

fun areConsecutiveEligibleDays(
    date1: LocalDate,
    date2: LocalDate,
    eligibleWeekdays: Set<DayOfWeek>,
): Boolean {
    var checkDate = date1.plus(1, DateTimeUnit.DAY)
    while (checkDate < date2) {
        if (eligibleWeekdays.contains(checkDate.dayOfWeek)) {
            return false
        }
        checkDate = checkDate.plus(1, DateTimeUnit.DAY)
    }
    return checkDate == date2
}

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
