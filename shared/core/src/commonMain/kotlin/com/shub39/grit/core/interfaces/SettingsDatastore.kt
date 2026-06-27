package com.shub39.grit.core.interfaces

import com.shub39.grit.core.habits.TimeDivision
import com.shub39.grit.core.settings.Sections
import com.shub39.grit.core.tasks.PomodoroSettings
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