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

package com.loc.hexis.core.habits

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class WeeklyPoints(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val pointsEarned: Int,
    val habitsCompleted: Int,
    val totalPossiblePoints: Int,
    val completionRate: Float,
    val streakBonusPoints: Int = 0,
    val perfectWeekBonusPoints: Int = 0,
)
