package com.loc.hexis.core.data

import androidx.room3.ColumnTypeConverter
import kotlin.time.Instant
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

object Converters {
    val allDays = dayOfWeekToString(DayOfWeek.entries.toSet())

    @ColumnTypeConverter
    fun dayOfWeekToString(value: Set<DayOfWeek>): String {
        return value.joinToString(",") { it.name }
    }

    @ColumnTypeConverter
    fun dayOfWeekFromString(value: String): Set<DayOfWeek> {
        return if (value.isBlank()) emptySet()
        else value.split(",").map { DayOfWeek.valueOf(it) }.toSet()
    }

    @ColumnTypeConverter
    fun dateFromTimestamp(value: Long?): LocalDateTime? {
        return value?.let {
            Instant.fromEpochSeconds(value).toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }

    @ColumnTypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toInstant(TimeZone.currentSystemDefault())?.epochSeconds
    }

    @ColumnTypeConverter
    fun dayFromTimestamp(value: Long): LocalDate {
        return value.let { LocalDate.fromEpochDays(value) }
    }

    @ColumnTypeConverter
    fun dayToTimestamp(date: LocalDate): Long {
        return date.toEpochDays()
    }
}