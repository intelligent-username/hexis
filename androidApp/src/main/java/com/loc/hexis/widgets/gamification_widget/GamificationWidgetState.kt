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

package com.loc.hexis.widgets.gamification_widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.currentState
import androidx.glance.appwidget.state.updateAppWidgetState

object GamificationWidgetState {
    private const val KEY_LAST_REFRESH = "gamification_last_refresh"
    private const val KEY_SELECTED_SIZE = "gamification_selected_size"

    private val lastRefreshKey = longPreferencesKey(KEY_LAST_REFRESH)
    private val selectedSizeKey = stringPreferencesKey(KEY_SELECTED_SIZE)

    fun saveLastRefresh(context: Context, id: GlanceId, time: Long) {
        updateAppWidgetState(context, id) { it[lastRefreshKey] = time }
    }

    fun getLastRefresh(context: Context, id: GlanceId): Long? {
        return currentState<Preferences>(context, id)[lastRefreshKey]
    }

    fun saveSelectedSize(context: Context, id: GlanceId, size: String) {
        updateAppWidgetState(context, id) { it[selectedSizeKey] = size }
    }

    fun getSelectedSize(context: Context, id: GlanceId): String? {
        return currentState<Preferences>(context, id)[selectedSizeKey]
    }
}
