package com.loc.hexis.shared.ui.task

import com.loc.hexis.core.habits.HabitRepo
import com.loc.hexis.core.interfaces.ActivePomodoroSessionData
import com.loc.hexis.core.interfaces.PomodoroAlarm
import com.loc.hexis.core.interfaces.SettingsDatastore
import com.loc.hexis.core.interfaces.VibratorUtil
import com.loc.hexis.core.now
import com.loc.hexis.core.tasks.PomodoroRepo
import com.loc.hexis.core.tasks.PomodoroSession
import com.loc.hexis.core.tasks.PomodoroSettings
import com.loc.hexis.core.tasks.PomodoroStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.math.max
import kotlin.time.Clock
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

enum class PomodoroPhase {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK,
}

data class ActivePomodoroState(
    val phase: PomodoroPhase = PomodoroPhase.FOCUS,
    val isRunning: Boolean = false,
    val targetEndTimeMillis: Long? = null,
    val durationSeconds: Int = 25 * 60,
    val secondsRemaining: Int = 25 * 60,
    val cyclesCompleted: Int = 0,
    val currentSessionInBatch: Int = 1,
    val currentSessionId: Long? = null,
    val sessionStartTimeMillis: Long? = null,
    val linkedHabitId: Long? = null,
    val settings: PomodoroSettings = PomodoroSettings(),
    val todayStats: PomodoroStats? = null,
)

@Single
class PomodoroManager(
    @Provided private val repo: PomodoroRepo,
    @Provided private val habitRepo: HabitRepo,
    @Provided private val settingsDatastore: SettingsDatastore,
    @Provided private val pomodoroAlarm: PomodoroAlarm,
    @Provided private val vibrator: VibratorUtil,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(ActivePomodoroState())
    val state: StateFlow<ActivePomodoroState> = _state.asStateFlow()

    init {
        scope.launch {
            val loadedSettings = settingsDatastore.getPomodoroSettings().first()
            _state.update {
                it.copy(
                    settings = loadedSettings,
                    durationSeconds = (loadedSettings.focusMinutes * 60).toInt(),
                    secondsRemaining = (loadedSettings.focusMinutes * 60).toInt(),
                )
            }
            restoreAndSyncActiveSession()
            startTickerLoop()
        }

        repo.getTodayStatsFlow()
            .onEach { stats -> _state.update { it.copy(todayStats = stats) } }
            .launchIn(scope)
    }


    private suspend fun restoreAndSyncActiveSession() {
        val activeData = settingsDatastore.getActivePomodoroSessionData().first() ?: return
        val nowMs = Clock.System.now().toEpochMilliseconds()

        if (nowMs >= activeData.targetEndTimeMillis) {
            finalizeCompletedSession(
                sessionId = activeData.sessionId,
                focusMinutes = activeData.focusMinutes,
                linkedHabitId = activeData.linkedHabitId,
            )
            settingsDatastore.clearActivePomodoroSessionData()
            refreshTodayStats()
        } else {
            _state.update {
                it.copy(
                    phase = PomodoroPhase.FOCUS,
                    isRunning = true,
                    targetEndTimeMillis = activeData.targetEndTimeMillis,
                    durationSeconds = (activeData.focusMinutes * 60).toInt(),
                    secondsRemaining = max(0, ((activeData.targetEndTimeMillis - nowMs) / 1000).toInt()),
                    currentSessionId = activeData.sessionId,
                    linkedHabitId = activeData.linkedHabitId,
                )
            }
            pomodoroAlarm.schedule(activeData.targetEndTimeMillis)
        }
    }

    private fun startTickerLoop() {
        scope.launch {
            while (true) {
                delay(1000L)
                val currentState = _state.value
                if (currentState.isRunning && currentState.targetEndTimeMillis != null) {
                    val nowMs = Clock.System.now().toEpochMilliseconds()
                    val remaining = max(0, ((currentState.targetEndTimeMillis - nowMs) / 1000).toInt())
                    _state.update { it.copy(secondsRemaining = remaining) }
                    if (nowMs >= currentState.targetEndTimeMillis) {
                        onPhaseComplete()
                    }
                }
            }
        }
    }

    fun startSession(linkedHabitId: Long? = null) {
        scope.launch {
            savePartialSessionIfActive(closeSession = true)
            val nw = LocalDateTime.now()
            val nowMs = Clock.System.now().toEpochMilliseconds()
            val currentSettings = _state.value.settings
            val durationSec = (currentSettings.focusMinutes * 60).toInt()
            val targetEndMs = nowMs + durationSec * 1000L

            val newSessionId = repo.insertSession(
                PomodoroSession(
                    goalDurationMinutes = currentSettings.focusMinutes,
                    timeStarted = nw,
                    linkedHabitId = linkedHabitId,
                )
            )

            settingsDatastore.setActivePomodoroSessionData(
                ActivePomodoroSessionData(
                    sessionId = newSessionId,
                    startTimeIso = nw.toString(),
                    targetEndTimeMillis = targetEndMs,
                    focusMinutes = currentSettings.focusMinutes,
                    linkedHabitId = linkedHabitId,
                )
            )

            _state.update {
                it.copy(
                    phase = PomodoroPhase.FOCUS,
                    isRunning = true,
                    targetEndTimeMillis = targetEndMs,
                    durationSeconds = durationSec,
                    secondsRemaining = durationSec,
                    currentSessionId = newSessionId,
                    sessionStartTimeMillis = nowMs,
                    linkedHabitId = linkedHabitId,
                )
            }

            pomodoroAlarm.schedule(targetEndMs)
        }
    }

    fun pauseSession() {
        pomodoroAlarm.cancel()
        scope.launch {
            savePartialSessionIfActive(closeSession = false)
            settingsDatastore.clearActivePomodoroSessionData()
            _state.update {
                it.copy(
                    isRunning = false,
                    targetEndTimeMillis = null,
                )
            }
        }
    }

    fun resumeSession(linkedHabitId: Long? = null) {
        scope.launch {
            val currentState = _state.value
            val nowMs = Clock.System.now().toEpochMilliseconds()
            val targetEndMs = nowMs + currentState.secondsRemaining * 1000L

            var sessionId = currentState.currentSessionId
            val nw = LocalDateTime.now()

            if (sessionId == null && currentState.phase == PomodoroPhase.FOCUS) {
                sessionId = repo.insertSession(
                    PomodoroSession(
                        goalDurationMinutes = currentState.settings.focusMinutes,
                        timeStarted = nw,
                        linkedHabitId = linkedHabitId ?: currentState.linkedHabitId,
                    )
                )
            }

            if (currentState.phase == PomodoroPhase.FOCUS && sessionId != null) {
                settingsDatastore.setActivePomodoroSessionData(
                    ActivePomodoroSessionData(
                        sessionId = sessionId,
                        startTimeIso = nw.toString(),
                        targetEndTimeMillis = targetEndMs,
                        focusMinutes = currentState.settings.focusMinutes,
                        linkedHabitId = linkedHabitId ?: currentState.linkedHabitId,
                    )
                )
            }

            _state.update {
                it.copy(
                    isRunning = true,
                    targetEndTimeMillis = targetEndMs,
                    currentSessionId = sessionId,
                    sessionStartTimeMillis = nowMs,
                )
            }

            pomodoroAlarm.schedule(targetEndMs)
        }
    }

    fun onAlarmFired() {
        scope.launch {
            val currentState = _state.value
            if (currentState.isRunning && currentState.phase == PomodoroPhase.FOCUS) {
                onPhaseComplete()
            } else {
                restoreAndSyncActiveSession()
            }
        }
    }

    fun onPhaseComplete() {
        pomodoroAlarm.cancel()
        val currentState = _state.value
        vibrator.buzz()

        if (currentState.phase == PomodoroPhase.FOCUS) {
            val sessionId = currentState.currentSessionId
            val focusMins = currentState.settings.focusMinutes
            val linkedHabit = currentState.linkedHabitId

            scope.launch {
                if (sessionId != null) {
                    finalizeCompletedSession(sessionId, focusMins, linkedHabit)
                }
                settingsDatastore.clearActivePomodoroSessionData()
                refreshTodayStats()
            }

            val nextCycles = currentState.cyclesCompleted + 1
            val nextBatchCount: Int
            val nextPhase: PomodoroPhase
            val nextDurationSec: Int

            if (currentState.settings.longBreakInterval > 0 && currentState.currentSessionInBatch >= currentState.settings.longBreakInterval) {
                nextBatchCount = 1
                nextPhase = PomodoroPhase.LONG_BREAK
                nextDurationSec = (currentState.settings.longBreakMinutes * 60).toInt()
            } else {
                nextBatchCount = currentState.currentSessionInBatch + 1
                nextPhase = PomodoroPhase.SHORT_BREAK
                nextDurationSec = (currentState.settings.shortBreakMinutes * 60).toInt()
            }

            _state.update {
                it.copy(
                    phase = nextPhase,
                    isRunning = false,
                    targetEndTimeMillis = null,
                    durationSeconds = nextDurationSec,
                    secondsRemaining = nextDurationSec,
                    cyclesCompleted = nextCycles,
                    currentSessionInBatch = nextBatchCount,
                    currentSessionId = null,
                    sessionStartTimeMillis = null,
                )
            }
        } else {
            val focusSec = (currentState.settings.focusMinutes * 60).toInt()
            val resetCycles = if (currentState.phase == PomodoroPhase.LONG_BREAK) 0 else currentState.cyclesCompleted
            _state.update {
                it.copy(
                    phase = PomodoroPhase.FOCUS,
                    isRunning = false,
                    targetEndTimeMillis = null,
                    durationSeconds = focusSec,
                    secondsRemaining = focusSec,
                    cyclesCompleted = resetCycles,
                    currentSessionId = null,
                    sessionStartTimeMillis = null,
                )
            }
        }
    }

    private suspend fun finalizeCompletedSession(
        sessionId: Long,
        focusMinutes: Float,
        linkedHabitId: Long?,
    ) {
        val allSessions = repo.getAllSessions()
        val existingSession = allSessions.find { it.id == sessionId }

        if (existingSession != null && !existingSession.completed) {
            val now = LocalDateTime.now()
            repo.finishSession(sessionId, now, completed = true, focusMinutes)

            if (linkedHabitId != null) {
                val habit = habitRepo.getHabitById(linkedHabitId)
                if (habit != null) {
                    habitRepo.incrementHabitProgress(linkedHabitId, LocalDate.now(), habit.incrementBy)
                }
            }
        }
    }

    fun resetTimer() {
        pomodoroAlarm.cancel()
        scope.launch {
            savePartialSessionIfActive(closeSession = true)
            settingsDatastore.clearActivePomodoroSessionData()
            val currentSettings = _state.value.settings
            val durationSec = when (_state.value.phase) {
                PomodoroPhase.FOCUS -> (currentSettings.focusMinutes * 60).toInt()
                PomodoroPhase.SHORT_BREAK -> (currentSettings.shortBreakMinutes * 60).toInt()
                PomodoroPhase.LONG_BREAK -> (currentSettings.longBreakMinutes * 60).toInt()
            }
            _state.update {
                it.copy(
                    isRunning = false,
                    targetEndTimeMillis = null,
                    durationSeconds = durationSec,
                    secondsRemaining = durationSec,
                    currentSessionId = null,
                    sessionStartTimeMillis = null,
                )
            }
        }
    }

    fun skipBreak() {
        pomodoroAlarm.cancel()
        val focusSec = (_state.value.settings.focusMinutes * 60).toInt()
        _state.update {
            it.copy(
                phase = PomodoroPhase.FOCUS,
                isRunning = false,
                targetEndTimeMillis = null,
                durationSeconds = focusSec,
                secondsRemaining = focusSec,
            )
        }
    }

    private suspend fun savePartialSessionIfActive(closeSession: Boolean = true) {
        val id = _state.value.currentSessionId
        val startMs = _state.value.sessionStartTimeMillis
        if (id != null && startMs != null) {
            val nowMs = Clock.System.now().toEpochMilliseconds()
            val elapsedMinutes = max(0f, (nowMs - startMs) / 60000f)
            if (elapsedMinutes > 0.1f) {
                repo.finishSession(id, LocalDateTime.now(), completed = false, timeCompletedMinutes = elapsedMinutes)
            }
        }
        if (closeSession) {
            _state.update { it.copy(currentSessionId = null, sessionStartTimeMillis = null) }
        }
    }

    fun applyPomodoroSettings(newSettings: PomodoroSettings) {
        scope.launch {
            settingsDatastore.setPomodoroSettings(newSettings)
            val isFocus = _state.value.phase == PomodoroPhase.FOCUS
            val isRunning = _state.value.isRunning
            val focusSec = (newSettings.focusMinutes * 60).toInt()
            _state.update {
                it.copy(
                    settings = newSettings,
                    durationSeconds = if (!isRunning && isFocus) focusSec else it.durationSeconds,
                    secondsRemaining = if (!isRunning && isFocus) focusSec else it.secondsRemaining,
                )
            }
        }
    }

    private suspend fun refreshTodayStats() {
        val stats = repo.getTodayStats()
        _state.update { it.copy(todayStats = stats) }
    }
}
