package com.loc.hexis.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loc.hexis.core.habits.HabitRepo
import com.loc.hexis.core.interfaces.ChangelogManager
import com.loc.hexis.core.interfaces.SettingsDatastore
import com.loc.hexis.core.interfaces.ThemeDatastore
import com.loc.hexis.core.now
import com.loc.hexis.shared.ui.app.LaunchSource
import com.loc.hexis.shared.ui.app.MainAppState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class MainViewModel(
    @Provided private val themeDatastore: ThemeDatastore,
    @Provided private val settingsDatastore: SettingsDatastore,
    @Provided private val changelogManager: ChangelogManager,
    @Provided private val repo: HabitRepo,
) : ViewModel() {
    var observerJob: Job? = null

    private val _state = MutableStateFlow(MainAppState())

    val state =
        _state
            .asStateFlow()
            .onStart {
                checkChangelog()
                observeDatastore()
                computeLoadingStats()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = MainAppState(),
            )

    fun setAppUnlocked(value: Boolean) {
        _state.update { it.copy(isAppUnlocked = value) }
    }

    fun setBiometricLock(value: Boolean) {
        viewModelScope.launch { settingsDatastore.setBiometricPref(value) }
    }

    private fun observeDatastore() {
        observerJob?.cancel()
        observerJob =
            viewModelScope.launch {
                combine(
                        themeDatastore.getPaletteStyle(),
                        themeDatastore.getSeedColorFlow(),
                        themeDatastore.getFontPrefFlow(),
                        themeDatastore.getMaterialYouFlow(),
                        themeDatastore.getAppThemeFlow(),
                    ) { palette, seedColor, font, materialYou, appTheme ->
                        _state.update {
                            it.copy(
                                theme =
                                    it.theme.copy(
                                        paletteStyle = palette,
                                        seedColor = seedColor,
                                        font = font,
                                        isMaterialYou = materialYou,
                                        appTheme = appTheme,
                                    )
                            )
                        }
                    }
                    .launchIn(this)

                themeDatastore
                    .getAmoledPref()
                    .onEach { pref ->
                        _state.update { it.copy(theme = it.theme.copy(isAmoled = pref)) }
                    }
                    .launchIn(this)

                // Read startingSection eagerly so the initial page is correct before
                // the UI is shown, then subscribe for ongoing changes.
                val initialSection = settingsDatastore.getStartingSectionPref().first()
                _state.update { it.copy(startingSection = initialSection) }
                settingsDatastore
                    .getStartingSectionPref()
                    .onEach { pref -> _state.update { it.copy(startingSection = pref) } }
                    .launchIn(this)

                settingsDatastore
                    .getBiometricLockPref()
                    .onEach { pref ->
                        _state.update { it.copy(isBiometricLockOn = pref, isAppUnlocked = false) }
                    }
                    .launchIn(this)
            }
    }

    private fun computeLoadingStats() {
        viewModelScope.launch {
            var firstLaunch = settingsDatastore.getFirstLaunchDate().first()
            if (firstLaunch == null) {
                firstLaunch = LocalDate.now()
                settingsDatastore.setFirstLaunchDate(firstLaunch)
            }
            val days = firstLaunch.daysUntil(LocalDate.now()) + 1

            val trend = repo.getPointsTrend().first()
            val points = trend.currentPartialPoints

            _state.update { it.copy(dayOnHexis = days, weeklyPoints = points) }
        }
    }

    private fun checkChangelog() {
        viewModelScope.launch {
            val changeLogs = changelogManager.changelogs.first()
            val lastShownChangelog = settingsDatastore.getLastChangelogShown().first()

            if (lastShownChangelog != changeLogs.firstOrNull()?.version) {
                _state.update { it.copy(currentChangelog = changeLogs.firstOrNull()) }
            }
        }
    }

    fun dismissChangelog() {
        _state.value.currentChangelog?.version?.let {
            viewModelScope.launch { settingsDatastore.updateLastChangelogShown(it) }
        }
        _state.update { it.copy(currentChangelog = null) }
    }

    fun setShortcutAction(action: String?, noteId: Long? = null) {
        _state.update { it.copy(shortcutAction = action, targetNoteId = noteId) }
    }

    fun setLaunchSource(source: LaunchSource) {
        _state.update { it.copy(launchSource = source) }
    }
}
