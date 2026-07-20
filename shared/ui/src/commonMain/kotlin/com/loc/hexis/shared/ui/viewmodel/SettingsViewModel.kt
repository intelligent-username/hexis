package com.loc.hexis.shared.ui.viewmodel

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loc.hexis.core.interfaces.BiometricUtils
import com.loc.hexis.core.interfaces.ChangelogManager
import com.loc.hexis.core.interfaces.SettingsDatastore
import com.loc.hexis.core.interfaces.ThemeDatastore
import com.loc.hexis.core.settings.backup.ExportRepo
import com.loc.hexis.core.settings.backup.ExportState
import com.loc.hexis.core.settings.backup.RestoreRepo
import com.loc.hexis.core.settings.backup.RestoreResult
import com.loc.hexis.core.settings.backup.RestoreState
import com.loc.hexis.shared.ui.setting.BackupState
import com.loc.hexis.shared.ui.setting.SettingsAction
import com.loc.hexis.shared.ui.setting.SettingsAction.ChangeAmoled
import com.loc.hexis.shared.ui.setting.SettingsAction.ChangeAppTheme
import com.loc.hexis.shared.ui.setting.SettingsAction.ChangeBiometricLock
import com.loc.hexis.shared.ui.setting.SettingsAction.ChangeFontPref
import com.loc.hexis.shared.ui.setting.SettingsAction.ChangeIs24Hr
import com.loc.hexis.shared.ui.setting.SettingsAction.ChangeMaterialYou
import com.loc.hexis.shared.ui.setting.SettingsAction.ChangePaletteStyle
import com.loc.hexis.shared.ui.setting.SettingsAction.ChangePauseNotifications
import com.loc.hexis.shared.ui.setting.SettingsAction.ChangeReorderHabits
import com.loc.hexis.shared.ui.setting.SettingsAction.ChangeReorderTasks
import com.loc.hexis.shared.ui.setting.SettingsAction.ChangeSeedColor
import com.loc.hexis.shared.ui.setting.SettingsAction.ChangeStartOfTheWeek
import com.loc.hexis.shared.ui.setting.SettingsAction.ChangeStartingPage
import com.loc.hexis.shared.ui.setting.SettingsAction.OnExport
import com.loc.hexis.shared.ui.setting.SettingsAction.OnResetBackupState
import com.loc.hexis.shared.ui.setting.SettingsAction.OnRestore
import com.loc.hexis.shared.ui.setting.SettingsState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class SettingsViewModel(
    @Provided private val exportRepo: ExportRepo,
    @Provided private val restoreRepo: RestoreRepo,
    @Provided private val themeDatastore: ThemeDatastore,
    @Provided private val settingsDatastore: SettingsDatastore,
    @Provided private val changelogManager: ChangelogManager,
    @Provided private val biometricUtils: BiometricUtils,
) : ViewModel() {
    private var observeJob: Job? = null

    private val _state = MutableStateFlow(SettingsState())

    val state =
        _state
            .asStateFlow()
            .onStart {
                observeJob()
                getChangeLogs()
                getBiometricStatus()
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())

    fun onAction(action: SettingsAction) =
        viewModelScope.launch {
            when (action) {
                is ChangeAmoled -> themeDatastore.setAmoledPref(action.pref)

                is ChangeAppTheme -> themeDatastore.setAppTheme(action.appTheme)

                is ChangeIs24Hr -> settingsDatastore.setIs24Hr(action.pref)

                is ChangeMaterialYou -> themeDatastore.setMaterialYou(action.pref)

                is ChangePaletteStyle -> themeDatastore.setPaletteStyle(action.style)

                is ChangeSeedColor -> themeDatastore.setSeedColor(action.color.toArgb())

                is ChangeStartOfTheWeek -> settingsDatastore.setStartOfWeek(action.pref)

                is ChangeStartingPage -> settingsDatastore.setStartingPage(action.page)

                OnResetBackupState -> {
                    _state.update { it.copy(backupState = BackupState()) }
                }

                OnExport -> {
                    _state.update {
                        it.copy(
                            backupState = it.backupState.copy(exportState = ExportState.EXPORTING)
                        )
                    }

                    exportRepo.exportToJson()

                    _state.update {
                        it.copy(
                            backupState = it.backupState.copy(exportState = ExportState.EXPORTED)
                        )
                    }
                }

                OnRestore -> {
                    _state.update {
                        it.copy(
                            backupState = it.backupState.copy(restoreState = RestoreState.RESTORING)
                        )
                    }

                    val result = restoreRepo.restoreData()

                    _state.update {
                        it.copy(
                            backupState =
                                it.backupState.copy(
                                    restoreState =
                                        when (result) {
                                            is RestoreResult.Failure -> RestoreState.FAILURE
                                            RestoreResult.Success -> RestoreState.RESTORED
                                        }
                                )
                        )
                    }
                }

                is ChangePauseNotifications -> settingsDatastore.setNotifications(action.pref)

                is ChangeFontPref -> themeDatastore.setFontPref(action.font)

                is ChangeBiometricLock -> settingsDatastore.setBiometricPref(action.pref)

                is ChangeReorderTasks -> settingsDatastore.setTaskReorderPref(action.pref)

                is ChangeReorderHabits -> settingsDatastore.setHabitReorderPref(action.pref)
            }
        }

    private fun getBiometricStatus() {
        _state.update {
            it.copy(isBiometricLockAvailable = biometricUtils.authenticationAvailable())
        }
    }

    private fun getChangeLogs() {
        changelogManager.changelogs
            .onEach { logs ->
                _state.update {
                    it.copy(changelog = logs, currentVersion = logs.firstOrNull()?.version)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeJob() {
        observeJob?.cancel()
        observeJob =
            viewModelScope.launch {
                settingsDatastore
                    .getTaskReorderPref()
                    .onEach { pref -> _state.update { it.copy(reorderTasks = pref) } }
                    .launchIn(this)

                settingsDatastore
                    .getHabitReorderPref()
                    .onEach { pref -> _state.update { it.copy(reorderHabits = pref) } }
                    .launchIn(this)

                settingsDatastore
                    .getNotificationsFlow()
                    .onEach { pref -> _state.update { it.copy(pauseNotifications = pref) } }
                    .launchIn(this)

                themeDatastore
                    .getAppThemeFlow()
                    .onEach { theme ->
                        _state.update { it.copy(theme = it.theme.copy(appTheme = theme)) }
                    }
                    .launchIn(this)

                themeDatastore
                    .getFontPrefFlow()
                    .onEach { font ->
                        _state.update { it.copy(theme = it.theme.copy(font = font)) }
                    }
                    .launchIn(this)

                themeDatastore
                    .getSeedColorFlow()
                    .onEach { seedColor ->
                        _state.update { it.copy(theme = it.theme.copy(seedColor = seedColor)) }
                    }
                    .launchIn(this)

                themeDatastore
                    .getAmoledPref()
                    .onEach { isAmoled ->
                        _state.update { it.copy(theme = it.theme.copy(isAmoled = isAmoled)) }
                    }
                    .launchIn(this)

                themeDatastore
                    .getMaterialYouFlow()
                    .onEach { isMaterialYou ->
                        _state.update {
                            it.copy(theme = it.theme.copy(isMaterialYou = isMaterialYou))
                        }
                    }
                    .launchIn(this)

                themeDatastore
                    .getPaletteStyle()
                    .onEach { paletteStyle ->
                        _state.update {
                            it.copy(theme = it.theme.copy(paletteStyle = paletteStyle))
                        }
                    }
                    .launchIn(this)

                settingsDatastore
                    .getIs24Hr()
                    .onEach { is24Hr -> _state.update { it.copy(is24Hr = is24Hr) } }
                    .launchIn(this)

                settingsDatastore
                    .getStartingSectionPref()
                    .onEach { startingPage ->
                        _state.update { it.copy(startingPage = startingPage) }
                    }
                    .launchIn(this)

                settingsDatastore
                    .getStartOfTheWeekPref()
                    .onEach { startOfTheWeek ->
                        _state.update { it.copy(startOfTheWeek = startOfTheWeek) }
                    }
                    .launchIn(this)

                settingsDatastore
                    .getBiometricLockPref()
                    .onEach { isBiometricLockOn ->
                        _state.update { it.copy(isBiometricLockOn = isBiometricLockOn) }
                    }
                    .launchIn(this)
            }
    }
}
