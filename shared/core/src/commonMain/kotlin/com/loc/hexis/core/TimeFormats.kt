package com.loc.hexis.core

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char

fun LocalDateTime.toFormattedString(is24Hr: Boolean): String {
    return this.format(
        LocalDateTime.Format {
            day()
            char(' ')
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            char(' ')
            year()
            chars(" @ ")
            if (is24Hr) hour() else amPmHour()
            char(':')
            minute()
            char(' ')
            if (!is24Hr) amPmMarker(am = "AM", pm = "PM")
        }
    )
}

fun LocalDate.toFormattedString(): String {
    return this.format(
        LocalDate.Format {
            day()
            char(' ')
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            char(' ')
            year()
        }
    )
}

fun LocalTime.toFormattedString(is24Hr: Boolean): String {
    return this.format(
        LocalTime.Format {
            if (is24Hr) hour() else amPmHour()
            char(':')
            minute()
            char(' ')
            if (!is24Hr) amPmMarker(am = "AM", pm = "PM")
        }
    )
}

fun YearMonth.toFormattedString(): String {
    return this.format(
        YearMonth.Format {
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            chars(" ")
            year()
        }
    )
}
