
package com.loc.hexis.core

import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

fun LocalDateTime.Companion.now(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun LocalDate.Companion.now(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

fun LocalTime.Companion.now(): LocalTime = LocalDateTime.now().time

fun getLogicalToday(isCutoffEnabled: Boolean = false, cutoffHour: Int = 4): LocalDate {
    val nowDateTime = LocalDateTime.now()
    return if (isCutoffEnabled && nowDateTime.hour < cutoffHour) {
        nowDateTime.date.minus(1, DateTimeUnit.DAY)
    } else {
        nowDateTime.date
    }
}
