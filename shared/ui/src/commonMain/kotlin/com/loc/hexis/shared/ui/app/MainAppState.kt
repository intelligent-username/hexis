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

package com.loc.hexis.shared.ui.app

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.loc.hexis.core.app.VersionEntry
import com.loc.hexis.core.settings.Sections
import com.loc.hexis.core.theme.Theme

enum class LaunchSource {
    LAUNCHER,
    WIDGET,
    SHORTCUT,
    NOTIFICATION,
    UNKNOWN,
}

@Stable
@Immutable
data class MainAppState(
    val isAppUnlocked: Boolean = false,
    val isBiometricLockOn: Boolean? = null,
    val startingSection: Sections = Sections.Tasks,
    val theme: Theme = Theme(),
    val currentChangelog: VersionEntry? = null,
    val shortcutAction: String? = null,
    val launchSource: LaunchSource = LaunchSource.UNKNOWN,
    val dayOnHexis: Int = 0,
    val weeklyPoints: Int = 0,
)
