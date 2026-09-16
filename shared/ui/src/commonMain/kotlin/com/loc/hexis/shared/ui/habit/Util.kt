
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

