package com.loc.hexis.shared.ui.habit

import kotlinx.datetime.DayOfWeek

fun daysStartingFrom(start: DayOfWeek): Set<DayOfWeek> {
    val allDays = DayOfWeek.entries
    val startIndex = start.ordinal

    return buildSet {
        for (i in allDays.indices) {
            add(allDays[(startIndex + i) % allDays.size])
        }
    }
}

fun getOrdinalSuffix(day: Int): String {
    return when {
        day in 11..13 -> "${day}th"
        else ->
            when (day % 10) {
                1 -> "${day}st"
                2 -> "${day}nd"
                3 -> "${day}rd"
                else -> "${day}th"
            }
    }
}