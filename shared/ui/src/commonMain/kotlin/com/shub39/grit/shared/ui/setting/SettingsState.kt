package com.shub39.grit.shared.ui.setting

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.shub39.grit.core.app.Changelog
import com.shub39.grit.core.settings.Sections
import com.shub39.grit.core.settings.backup.ExportState
import com.shub39.grit.core.settings.backup.RestoreState
import com.shub39.grit.core.theme.Theme
import kotlinx.datetime.DayOfWeek

@Stable
@Immutable
data class SettingsState(
    val changelog: Changelog = emptyList(),
    val currentVersion: String? = null,
    val backupState: BackupState = BackupState(),

    // datastore
    val theme: Theme = Theme(),
    val is24Hr: Boolean = false,
    val reorderTasks: Boolean = false,
    val startOfTheWeek: DayOfWeek = DayOfWeek.MONDAY,
    val pauseNotifications: Boolean = false,
    val startingPage: Sections = Sections.Tasks,
    val isBiometricLockOn: Boolean? = null,
    val isBiometricLockAvailable: Boolean = false,
)

@Stable
@Immutable
data class BackupState(
    val exportState: ExportState = ExportState.IDLE,
    val restoreState: RestoreState = RestoreState.IDLE,
)