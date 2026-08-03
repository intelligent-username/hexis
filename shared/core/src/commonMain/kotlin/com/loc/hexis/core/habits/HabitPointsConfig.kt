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

import kotlinx.serialization.Serializable

@Serializable
data class HabitPointsConfig(
    val habitId: Long,
    val basePoints: Int = 10,
    val streakBonusPerDay: Int = 2,
    val streakBonusCap: Int = 20,
    val perfectWeekBonus: Int = 50,
)
