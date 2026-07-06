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

package com.loc.hexis.habits.data.database

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "habit_index")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val index: Int,
    val days: Set<DayOfWeek>,
    val time: LocalDateTime,
    @ColumnInfo(name = "reminder", defaultValue = "1") val reminder: Boolean,
    @ColumnInfo(defaultValue = "CHECKBOX") val displayMode: String,
    @ColumnInfo(defaultValue = "1.0") val targetValue: Double?,
    @ColumnInfo(defaultValue = "0") val pomodoroLinked: Boolean,
    @ColumnInfo(defaultValue = "1.0") val incrementBy: Double,
)
