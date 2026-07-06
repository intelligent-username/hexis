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

package com.loc.hexis.core.interfaces

import com.loc.hexis.core.habits.TimeDivision
import com.loc.hexis.core.settings.Sections
import com.loc.hexis.core.tasks.PomodoroSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek

interface SettingsDatastore {
    fun getStartOfTheWeekPref(): Flow<DayOfWeek>

    suspend fun setStartOfWeek(day: DayOfWeek)

    fun getStartingSectionPref(): Flow<Sections>

    suspend fun setStartingPage(page: Sections)

    fun getIs24Hr(): Flow<Boolean>

    suspend fun setIs24Hr(pref: Boolean)

    fun getNotificationsFlow(): Flow<Boolean>

    suspend fun setNotifications(pref: Boolean)

    fun getBiometricLockPref(): Flow<Boolean>

    suspend fun setBiometricPref(pref: Boolean)

    fun getTaskReorderPref(): Flow<Boolean>

    suspend fun setTaskReorderPref(pref: Boolean)

    fun getCompactViewPref(): Flow<Boolean>

    suspend fun setCompactView(pref: Boolean)

    fun getLastChangelogShown(): Flow<String>

    suspend fun updateLastChangelogShown(version: String)

    fun getArchivedHabitIds(): Flow<Set<Long>>

    suspend fun setArchivedHabitIds(ids: Set<Long>)

    fun getTimeDivisions(): Flow<List<TimeDivision>>

    suspend fun setTimeDivisions(divisions: List<TimeDivision>)

    fun getHabitTimeDivisionMap(): Flow<Map<Long, Long>>

    suspend fun setHabitTimeDivision(habitId: Long, divisionId: Long?)

    fun getPomodoroSettings(): Flow<PomodoroSettings>

    suspend fun setPomodoroSettings(settings: PomodoroSettings)
}
