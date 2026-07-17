package com.loc.hexis.shared.ui.viewmodel

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loc.hexis.core.interfaces.BiometricUtils
import com.loc.hexis.core.interfaces.ChangelogManager
import com.loc.hexis.core.interfaces.SettingsDatastore
import com.loc.hexis.core.interfaces.ThemeDatastore
import com.loc.hexis.core.settings.backup.ExportRepo
import com.loc.hexis.core.settings.backup.RestoreRepo
import com.loc.hexis.shared.ui.setting.BackupState
import com.loc.hexis.shared.ui.setting.SettingsAction
import com.loc.hexis.shared.ui.setting.SettingsState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
                        it.copy(backupState = it.backupState.copy(exportState = EXPORTING))
                    }

                    exportRepo.exportToJson()

                    _state.update {
                        it.copy(backupState = it.backupState.copy(exportState = EXPORTED))
                    }
                }

                OnRestore -> {
                    _state.update {
                        it.copy(backupState = it.backupState.copy(restoreState = RESTORING))
                    }

                    val result = restoreRepo.restoreData()

                    _state.update {
                        it.copy(
                            backupState =
                                it.backupState.copy(
                                    restoreState =
                                        when (result) {
                                            is Failure -> FAILURE
                                            Success -> RESTORED
                                        }
                                )
                        )
                    }
                }

                is ChangePauseNotifications -> settingsDatastore.setNotifications(action.pref)

                is ChangeFontPref -> themeDatastore.setFontPref(action.font)

                is ChangeBiometricLock -> settingsDatastore.setBiometricPref(action.pref)

                is ChangeReorderTasks -> settingsDatastore.setTaskReorderPref(action.pref)
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
            combine(
                    settingsDatastore.getTaskReorderPref(),
                    settingsDatastore.getNotificationsFlow(),
                    themeDatastore.getAppThemeFlow(),
                    themeDatastore.getFontPrefFlow(),
                    themeDatastore.getSeedColorFlow(),
                    themeDatastore.getAmoledPref(),
                    themeDatastore.getMaterialYouFlow(),
                    themeDatastore.getPaletteStyle(),
                    settingsDatastore.getIs24Hr(),
                    settingsDatastore.getStartingSectionPref(),
                    settingsDatastore.getStartOfTheWeekPref(),
                    settingsDatastore.getBiometricLockPref(),
                ) { reorderTasks, pauseNotifications, appTheme, font, seedColor, isAmoled, isMaterialYou,
                    paletteStyle, is24Hr, startingPage, startOfTheWeek, isBiometricLockOn ->
                    _state.update {
                        it.copy(
                            reorderTasks = reorderTasks,
                            pauseNotifications = pauseNotifications,
                            theme =
                                it.theme.copy(
                                    appTheme = appTheme,
                                    font = font,
                                    seedColor = seedColor,
                                    isAmoled = isAmoled,
                                    isMaterialYou = isMaterialYou,
                                    paletteStyle = paletteStyle,
                                ),
                            is24Hr = is24Hr,
                            startingPage = startingPage,
                            startOfTheWeek = startOfTheWeek,
                            isBiometricLockOn = isBiometricLockOn,
                        )
                    }
                }
                .launchIn(viewModelScope)
    }
}
