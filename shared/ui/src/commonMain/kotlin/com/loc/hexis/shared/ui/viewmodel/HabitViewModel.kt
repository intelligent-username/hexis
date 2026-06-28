package com.loc.hexis.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loc.hexis.core.now
import com.loc.hexis.core.habits.Habit
import com.loc.hexis.core.habits.HabitRepo
import com.loc.hexis.core.interfaces.AlarmScheduler
import com.loc.hexis.core.interfaces.SettingsDatastore
import com.loc.hexis.shared.ui.habit.HabitState
import com.loc.hexis.shared.ui.habit.HabitsAction
import com.loc.hexis.shared.ui.habit.HabitsAction.*
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
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class HabitViewModel(
    @Provided private val scheduler: AlarmScheduler,
    @Provided private val repo: HabitRepo,
    @Provided private val datastore: SettingsDatastore,
) : ViewModel() {
    private var habitStatusJob: Job? = null
    private var overallAnalyticsJob: Job? = null
    private var observeDatastoreJob: Job? = null
    private var completedHabitsFetchJob: Job? = null

    private val _state = MutableStateFlow(HabitState())

    val state =
        _state
            .asStateFlow()
            .onStart {
                observeDataStore()
                observeHabitStatuses()
                observeOverallAnalytics()

                rescheduleAllHabits()
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabitState())

    // handles actions from habit page
    fun onAction(action: HabitsAction) {
        viewModelScope.launch {
            when (action) {
                is AddHabit -> upsertHabit(action.habit)

                is DeleteHabit -> deleteHabit(action.habit)

                is InsertStatus -> toggleHabitProgress(action.habit, action.date)

                is HabitsAction.ToggleHabitProgress -> toggleHabitProgress(action.habit, action.date)

                is HabitsAction.DecrementHabitProgress -> decrementHabitProgress(action.habit, action.date)

                is HabitsAction.IncrementHabitProgress -> incrementHabitProgress(action.habit, action.date)

                is UpdateHabit -> upsertHabit(action.habit)

                ReorderHabits -> {
                    _state.update { it.copy(isReordering = true) }
                    val currentList =
                        _state.value.habitsWithAnalytics.mapIndexed { index, analytics ->
                            analytics.habit.copy(index = index)
                        }

                    launch {
                        try {
                            currentList.forEach { upsertHabit(it) }
                        } finally {
                            _state.update { it.copy(isReordering = false) }
                        }
                    }
                }

                is PrepareAnalytics -> {
                    _state.update { it.copy(analyticsHabitId = action.habit?.id) }
                }

                OnAddHabitClicked -> {
                    _state.update { it.copy(showHabitAddSheet = true) }
                }

                DismissAddHabitDialog -> _state.update { it.copy(showHabitAddSheet = false) }

                is OnToggleCompactView -> datastore.setCompactView(action.pref)

                is OnToggleEditState -> _state.update { it.copy(editState = action.pref) }

                is OnTransientHabitReorder -> {
                    if (action.from == action.to) return@launch
                    val currentState = _state.value
                    val filteredList = currentState.habitsWithAnalytics.filter {
                        (it.habit.id in currentState.archivedHabitIds) == currentState.showArchivedHabits
                    }
                    if (action.from in filteredList.indices && action.to in filteredList.indices) {
                        val fromItem = filteredList[action.from]
                        val toItem = filteredList[action.to]

                        val currentList = currentState.habitsWithAnalytics.toMutableList()
                        val fromIndexUnfiltered = currentList.indexOf(fromItem)
                        currentList.removeAt(fromIndexUnfiltered)

                        val toIndexUnfiltered = currentList.indexOf(toItem)
                        val insertIndex = if (action.from < action.to) toIndexUnfiltered + 1 else toIndexUnfiltered

                        currentList.add(insertIndex, fromItem)
                        _state.update { it.copy(habitsWithAnalytics = currentList) }
                    }
                }

                is FetchCompletedHabitsForDate -> {
                    completedHabitsFetchJob?.cancel()
                    completedHabitsFetchJob = launch {
                        if (action.date == null) {
                            _state.update {
                                it.copy(
                                    overallAnalytics = it.overallAnalytics.copy(completedHabits = null)
                                )
                            }
                            return@launch
                        }

                        val completedHabits =
                            repo.getCompletedHabitsForDate(action.date).map { it.title }

                        _state.update { habitState ->
                            habitState.copy(
                                overallAnalytics =
                                    habitState.overallAnalytics.copy(
                                        completedHabits =
                                            if (completedHabits.isNotEmpty()) {
                                                action.date to completedHabits
                                            } else null
                                    )
                            )
                        }
                    }
                }

                is ToggleArchiveHabit -> {
                    val currentArchived = _state.value.archivedHabitIds.toMutableSet()
                    if (action.isArchived) currentArchived.add(action.id)
                    else currentArchived.remove(action.id)
                    datastore.setArchivedHabitIds(currentArchived)
                }

                is ToggleShowArchivedHabits -> {
                    _state.update { it.copy(showArchivedHabits = action.show) }
                }

                is ToggleOverallAnalytics -> {
                    _state.update { it.copy(showOverallAnalytics = action.show) }
                }

                is AddTimeDivision -> {
                    val currentList = _state.value.timeDivisions.toMutableList()
                    val newDivision = action.division.copy(id = LocalDateTime.now().toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
                    currentList.add(newDivision)
                    datastore.setTimeDivisions(currentList)
                }

                is UpdateTimeDivision -> {
                    val currentList = _state.value.timeDivisions.toMutableList()
                    val index = currentList.indexOfFirst { it.id == action.division.id }
                    if (index != -1) {
                        currentList[index] = action.division
                        datastore.setTimeDivisions(currentList)
                    }
                }

                is DeleteTimeDivision -> {
                    val currentList = _state.value.timeDivisions.toMutableList()
                    currentList.removeAll { it.id == action.id }
                    datastore.setTimeDivisions(currentList)
                    
                    val map = _state.value.habitTimeDivisionMap
                    val affectedHabits = map.filterValues { it == action.id }.keys
                    affectedHabits.forEach { habitId ->
                        datastore.setHabitTimeDivision(habitId, null)
                    }
                    if (_state.value.selectedTimeDivisionId == action.id) {
                        _state.update { it.copy(selectedTimeDivisionId = null) }
                    }
                }

                is ReorderTimeDivisions -> {
                    val currentList = action.mapping.map { it.second }.toMutableList()
                    datastore.setTimeDivisions(currentList)
                }

                is SetHabitTimeDivision -> {
                    datastore.setHabitTimeDivision(action.habitId, action.divisionId)
                }

                is SelectTimeDivision -> {
                    _state.update { it.copy(selectedTimeDivisionId = action.divisionId) }
                }

                is ToggleTimeDivisionSheet -> {}
            }
        }
    }

    private fun observeHabitStatuses() {
        habitStatusJob?.cancel()
        habitStatusJob =
            viewModelScope.launch {
                combine(repo.getHabitsWithAnalytics(), repo.getCompletedHabitIds()) { habits,
                                                                                      completedHabits ->
                    _state.update {
                        if (it.isReordering) it else it.copy(
                            habitsWithAnalytics = habits,
                            completedHabitIds = completedHabits,
                        )
                    }
                }
                    .launchIn(this)
            }
    }

    private fun observeOverallAnalytics() {
        overallAnalyticsJob?.cancel()
        overallAnalyticsJob =
            repo
                .getOverallAnalytics()
                .onEach { overallAnalytics ->
                    _state.update { it.copy(overallAnalytics = overallAnalytics) }
                }
                .launchIn(viewModelScope)
    }

    private fun observeDataStore() {
        observeDatastoreJob?.cancel()
        observeDatastoreJob =
            viewModelScope.launch {
                datastore
                    .getCompactViewPref()
                    .onEach { pref -> _state.update { it.copy(compactHabitView = pref) } }
                    .launchIn(this)

                datastore
                    .getStartOfTheWeekPref()
                    .onEach { pref -> _state.update { it.copy(startingDay = pref) } }
                    .launchIn(this)

                datastore
                    .getIs24Hr()
                    .onEach { pref -> _state.update { it.copy(is24Hr = pref) } }
                    .launchIn(this)

                datastore
                    .getArchivedHabitIds()
                    .onEach { pref -> _state.update { it.copy(archivedHabitIds = pref) } }
                    .launchIn(this)
                    
                datastore
                    .getTimeDivisions()
                    .onEach { list -> _state.update { it.copy(timeDivisions = list) } }
                    .launchIn(this)

                datastore
                    .getHabitTimeDivisionMap()
                    .onEach { map -> _state.update { it.copy(habitTimeDivisionMap = map) } }
                    .launchIn(this)
            }
    }

    private suspend fun rescheduleAllHabits() {
        repo.getHabits().forEach { habit -> scheduler.schedule(habit) }
    }

    private suspend fun upsertHabit(habit: Habit) {
        repo.upsertHabit(habit)
        scheduler.schedule(habit)
    }

    private suspend fun deleteHabit(habit: Habit) {
        repo.deleteHabit(habit.id)
        scheduler.cancel(habit)
    }

    private suspend fun insertHabitStatus(habit: Habit, date: LocalDate) {
        toggleHabitProgress(habit, date)
    }

    private suspend fun toggleHabitProgress(habit: Habit, date: LocalDate) {
        if (habit.pomodoroLinked) return
        val currentValue = repo.getHabitProgress(habit.id, date)
        if (currentValue >= (habit.targetValue ?: 1.0)) {
            repo.deleteHabitStatus(habit.id, date)
        } else {
            repo.incrementHabitProgress(habit.id, date, habit.incrementBy)
        }
    }

    private suspend fun decrementHabitProgress(habit: Habit, date: LocalDate) {
        repo.decrementHabitProgress(habit.id, date, habit.incrementBy)
    }

    private suspend fun incrementHabitProgress(habit: Habit, date: LocalDate) {
        repo.incrementHabitProgress(habit.id, date, habit.incrementBy)
    }
}