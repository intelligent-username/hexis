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

enum class WeeklyTimePeriod {
    DAYS_7,
    MONTHS_2,
    MONTHS_6,
    YEARS_1;

    companion object {
        fun WeeklyTimePeriod.toWeeks(): Int {
            return when (this) {
                DAYS_7 -> 1
                MONTHS_2 -> 8
                MONTHS_6 -> 26
                YEARS_1 -> 52
            }
        }

        fun WeeklyTimePeriod.toDisplayString(): String {
            return when (this) {
                DAYS_7 -> "7D"
                MONTHS_2 -> "2M"
                MONTHS_6 -> "6M"
                YEARS_1 -> "1Y"
            }
        }
    }
}
