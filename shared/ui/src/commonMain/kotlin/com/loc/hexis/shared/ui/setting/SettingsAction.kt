package com.loc.hexis.shared.ui.setting

import androidx.compose.ui.graphics.Color
import com.loc.hexis.core.settings.Sections
import com.loc.hexis.core.theme.AppTheme
import com.loc.hexis.core.theme.Fonts
import com.loc.hexis.core.theme.PaletteStyle
import kotlinx.datetime.DayOfWeek

sealed interface SettingsAction {
    data object OnResetBackupState : SettingsAction

    data object OnExport : SettingsAction

    data object OnRestore : SettingsAction

    data class ChangeStartOfTheWeek(val pref: DayOfWeek) : SettingsAction

    data class ChangeIs24Hr(val pref: Boolean) : SettingsAction

    data class ChangeStartingPage(val page: Sections) : SettingsAction

    data class ChangePauseNotifications(val pref: Boolean) : SettingsAction

    data class ChangeReorderTasks(val pref: Boolean) : SettingsAction

    data class ChangeAppTheme(val appTheme: AppTheme) : SettingsAction

    data class ChangeFontPref(val font: Fonts) : SettingsAction

    data class ChangeSeedColor(val color: Color) : SettingsAction

    data class ChangeAmoled(val pref: Boolean) : SettingsAction

    data class ChangePaletteStyle(val style: PaletteStyle) : SettingsAction

    data class ChangeMaterialYou(val pref: Boolean) : SettingsAction

    data class ChangeBiometricLock(val pref: Boolean) : SettingsAction
}