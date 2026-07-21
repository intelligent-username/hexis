package com.loc.hexis.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.loc.hexis.core.habits.TimeDivision
import com.loc.hexis.core.interfaces.SettingsDatastore
import com.loc.hexis.core.settings.Sections
import com.loc.hexis.core.tasks.PomodoroSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single(binds = [SettingsDatastore::class])
class SettingsDatastoreImpl(private val datastore: DataStore<Preferences>) : SettingsDatastore {

    companion object {
        private val startOfWeekKey = stringPreferencesKey("start_of_week")
        private val startingSectionKey = stringPreferencesKey("starting_page")
        private val is24HrKey = booleanPreferencesKey("is_24Hr")
        private val notificationsKey = booleanPreferencesKey("notifications")
        private val biometricLockKey = booleanPreferencesKey("biometric")
        private val taskReorderKey = booleanPreferencesKey("task_reorder")
        private val habitReorderKey = booleanPreferencesKey("habit_reorder")
        private val compactHabitView = booleanPreferencesKey("compact_habit_view")
        private val lastChangelogShownKey = stringPreferencesKey("last_changelog_shown")
        private val archivedHabitIdsKey = stringSetPreferencesKey("archived_habit_ids")
        private val timeDivisionsKey = stringPreferencesKey("time_divisions")
        private val habitTimeDivisionMapKey = stringPreferencesKey("habit_time_division_map")
        private val pomodoroSettingsKey = stringPreferencesKey("pomodoro_settings")
        private val firstLaunchDateKey = stringPreferencesKey("first_launch_date")
        private val lockVaultNotesKey = booleanPreferencesKey("lock_vault_notes")
        private val vaultPasswordHashKey = stringPreferencesKey("vault_password_hash")
    }

    override fun getStartOfTheWeekPref(): Flow<DayOfWeek> =
        datastore.data.map { prefs ->
            val dayOfWeek = prefs[startOfWeekKey] ?: DayOfWeek.MONDAY.name
            return@map DayOfWeek.valueOf(dayOfWeek)
        }

    override suspend fun setStartOfWeek(day: DayOfWeek) {
        datastore.edit { prefs -> prefs[startOfWeekKey] = day.name }
    }

    override fun getStartingSectionPref(): Flow<Sections> =
        datastore.data.map { pref ->
            val page = pref[startingSectionKey] ?: Sections.Tasks.name
            return@map Sections.valueOf(page)
        }

    override suspend fun setStartingPage(page: Sections) {
        datastore.edit { prefs -> prefs[startingSectionKey] = page.name }
    }

    override fun getIs24Hr(): Flow<Boolean> =
        datastore.data.map { prefs -> prefs[is24HrKey] == true }

    override suspend fun setIs24Hr(pref: Boolean) {
        datastore.edit { prefs -> prefs[is24HrKey] = pref }
    }

    override fun getNotificationsFlow(): Flow<Boolean> =
        datastore.data.map { prefs -> prefs[notificationsKey] == true }

    override suspend fun setNotifications(pref: Boolean) {
        datastore.edit { prefs -> prefs[notificationsKey] = pref }
    }

    override fun getBiometricLockPref(): Flow<Boolean> =
        datastore.data.map { prefs -> prefs[biometricLockKey] == true }

    override suspend fun setBiometricPref(pref: Boolean) {
        datastore.edit { prefs -> prefs[biometricLockKey] = pref }
    }

    override fun getTaskReorderPref(): Flow<Boolean> =
        datastore.data.map { prefs -> prefs[taskReorderKey] ?: true }

    override suspend fun setTaskReorderPref(pref: Boolean) {
        datastore.edit { prefs -> prefs[taskReorderKey] = pref }
    }

    override fun getHabitReorderPref(): Flow<Boolean> =
        datastore.data.map { prefs -> prefs[habitReorderKey] ?: false }

    override suspend fun setHabitReorderPref(pref: Boolean) {
        datastore.edit { prefs -> prefs[habitReorderKey] = pref }
    }

    override fun getCompactViewPref(): Flow<Boolean> =
        datastore.data.map { pref -> pref[compactHabitView] ?: false }

    override suspend fun setCompactView(pref: Boolean) {
        datastore.edit { prefs -> prefs[compactHabitView] = pref }
    }

    override fun getLastChangelogShown(): Flow<String> =
        datastore.data.map { prefs -> prefs[lastChangelogShownKey] ?: "" }

    override suspend fun updateLastChangelogShown(version: String) {
        datastore.edit { settings -> settings[lastChangelogShownKey] = version }
    }

    override fun getArchivedHabitIds(): Flow<Set<Long>> =
        datastore.data.map { prefs ->
            prefs[archivedHabitIdsKey]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        }

    override suspend fun setArchivedHabitIds(ids: Set<Long>) {
        datastore.edit { prefs -> prefs[archivedHabitIdsKey] = ids.map { it.toString() }.toSet() }
    }

    override fun getTimeDivisions(): Flow<List<TimeDivision>> =
        datastore.data.map { prefs ->
            prefs[timeDivisionsKey]?.let { Json.decodeFromString(it) } ?: emptyList()
        }

    override suspend fun setTimeDivisions(divisions: List<TimeDivision>) {
        datastore.edit { prefs -> prefs[timeDivisionsKey] = Json.encodeToString(divisions) }
    }

    override fun getHabitTimeDivisionMap(): Flow<Map<Long, Long>> =
        datastore.data.map { prefs ->
            prefs[habitTimeDivisionMapKey]
                ?.let { Json.decodeFromString<Map<String, Long>>(it) }
                ?.mapKeys { it.key.toLong() } ?: emptyMap()
        }

    override suspend fun setHabitTimeDivision(habitId: Long, divisionId: Long?) {
        datastore.edit { prefs ->
            val current =
                prefs[habitTimeDivisionMapKey]
                    ?.let { Json.decodeFromString<Map<String, Long>>(it) }
                    ?.toMutableMap() ?: mutableMapOf()
            if (divisionId != null) current[habitId.toString()] = divisionId
            else current.remove(habitId.toString())
            prefs[habitTimeDivisionMapKey] = Json.encodeToString(current)
        }
    }

    override fun getPomodoroSettings(): Flow<PomodoroSettings> =
        datastore.data.map { prefs ->
            prefs[pomodoroSettingsKey]?.let { Json.decodeFromString(it) } ?: PomodoroSettings()
        }

    override suspend fun setPomodoroSettings(settings: PomodoroSettings) {
        datastore.edit { prefs -> prefs[pomodoroSettingsKey] = Json.encodeToString(settings) }
    }

    override suspend fun setHabitTimeDivisionMap(map: Map<Long, Long>) {
        datastore.edit { prefs ->
            prefs[habitTimeDivisionMapKey] = Json.encodeToString(map.mapKeys { it.key.toString() })
        }
    }

    override fun getFirstLaunchDate(): Flow<LocalDate?> =
        datastore.data.map { prefs -> prefs[firstLaunchDateKey]?.let { LocalDate.parse(it) } }

    override suspend fun setFirstLaunchDate(date: LocalDate) {
        datastore.edit { prefs -> prefs[firstLaunchDateKey] = date.toString() }
    }

    override fun getLockVaultNotesPref(): Flow<Boolean> =
        datastore.data.map { prefs -> prefs[lockVaultNotesKey] == true }

    override suspend fun setLockVaultNotesPref(pref: Boolean) {
        datastore.edit { prefs -> prefs[lockVaultNotesKey] = pref }
    }

    override fun getVaultPasswordHash(): Flow<String?> =
        datastore.data.map { prefs -> prefs[vaultPasswordHashKey] }

    override suspend fun setVaultPasswordHash(hash: String?) {
        datastore.edit { prefs ->
            if (hash != null) prefs[vaultPasswordHashKey] = hash
            else prefs.remove(vaultPasswordHashKey)
        }
    }
}
