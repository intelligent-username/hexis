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

package com.loc.hexis.core.settings.backup

import com.loc.hexis.core.habits.TimeDivision
import com.loc.hexis.core.tasks.PomodoroSettings
import kotlinx.datetime.DayOfWeek

data class BackupSettings(
    val timeDivisions: List<TimeDivision>,
    val habitTimeDivisionMap: Map<Long, Long>,
    val archivedHabitIds: Set<Long>,
    val pomodoroSettings: PomodoroSettings?,
    val userSettings: BackupUserSettings?,
) {
    fun mergedWith(
        currentDivisions: List<TimeDivision>,
        currentAssignments: Map<Long, Long>,
        currentArchives: Set<Long>,
        currentVaultHash: String?,
    ): BackupSettings {
        val divisionsById = currentDivisions.associateBy { it.id }
        if (
            divisionsById.size != currentDivisions.size ||
                timeDivisions.any { divisionsById[it.id]?.let { old -> old != it } == true } ||
                habitTimeDivisionMap.any { (id, division) ->
                    currentAssignments[id]?.let { it != division } == true
                } ||
                (currentVaultHash != null &&
                    userSettings?.vaultPasswordHash?.let { it != currentVaultHash } == true)
        ) {
            throw BackupSettingsConflictException()
        }
        val divisions = currentDivisions + timeDivisions.filter { it.id !in divisionsById }
        val assignments = currentAssignments + habitTimeDivisionMap
        if (assignments.values.any { id -> divisions.none { it.id == id } }) {
            throw BackupSettingsConflictException()
        }
        return copy(
            timeDivisions = divisions,
            habitTimeDivisionMap = assignments,
            archivedHabitIds = currentArchives + archivedHabitIds,
            userSettings =
                userSettings?.copy(vaultPasswordHash = userSettings.vaultPasswordHash ?: currentVaultHash),
        )
    }
}

data class BackupUserSettings(
    val startOfTheWeek: DayOfWeek,
    val is24Hr: Boolean,
    val dayCutoffEnabled: Boolean,
    val dayCutoffHour: Int,
    val compactHabitView: Boolean,
    val taskReorderPref: Boolean,
    val habitReorderPref: Boolean,
    val putNewTasksAtTopPref: Boolean,
    val showPomodoroPieChartPref: Boolean,
    val lockVaultNotesPref: Boolean,
    val vaultPasswordHash: String?,
)

class BackupSettingsConflictException : Exception()
