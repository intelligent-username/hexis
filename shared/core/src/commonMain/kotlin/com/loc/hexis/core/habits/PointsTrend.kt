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
data class PointsTrend(
    val weeklyPoints: List<WeeklyPoints>,
    val currentWeekPoints: Int,
    val previousWeekPoints: Int,
    val netChange: Int,
    val netChangePercent: Float,
    val totalPointsAllTime: Int,
    val averageWeeklyPoints: Float,
    val bestWeekPoints: Int,
    val currentStreakWeeks: Int,
) {
    companion object {
        val empty = PointsTrend(
            weeklyPoints = emptyList(),
            currentWeekPoints = 0,
            previousWeekPoints = 0,
            netChange = 0,
            netChangePercent = 0f,
            totalPointsAllTime = 0,
            averageWeeklyPoints = 0f,
            bestWeekPoints = 0,
            currentStreakWeeks = 0,
        )
    }
}
