package com.shub39.grit.shared.ui.habit

import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.TimeDivision
import kotlinx.datetime.LocalDate

/**
 * Represents the set of user actions or events that can occur on the Habits screen. This sealed
 * interface is used to pass events from the UI to the ViewModel for processing.
 */
sealed interface HabitsAction {
    /** toggling the compact view mode for the habits list. */
    data class OnToggleCompactView(val pref: Boolean) : HabitsAction

    /** toggle habit reordering */
    data class OnToggleEditState(val pref: Boolean) : HabitsAction

    /** reorder a single habit [from]: from Index [to]: to Index */
    data class OnTransientHabitReorder(val from: Int, val to: Int) : HabitsAction

    data object DismissAddHabitDialog : HabitsAction

    /** open habit upsert sheet */
    data object OnAddHabitClicked : HabitsAction

    /** set [habit]'s id to view analytics for */
    data class PrepareAnalytics(val habit: Habit?) : HabitsAction

    data class AddHabit(val habit: Habit) : HabitsAction

    data class DeleteHabit(val habit: Habit) : HabitsAction

    /** Add/Remove status for [habit] at given [date] */
    data class InsertStatus(val habit: Habit, val date: LocalDate) : HabitsAction

    /** Increment/decrement progress for [habit] at given [date] */
    data class ToggleHabitProgress(val habit: Habit, val date: LocalDate) : HabitsAction

    data class UpdateHabit(val habit: Habit) : HabitsAction

    data object ReorderHabits : HabitsAction

    data class FetchCompletedHabitsForDate(val date: LocalDate?) : HabitsAction

    data class ToggleArchiveHabit(val id: Long, val isArchived: Boolean) : HabitsAction

    data class ToggleShowArchivedHabits(val show: Boolean) : HabitsAction

    data class ToggleOverallAnalytics(val show: Boolean) : HabitsAction

    data class AddTimeDivision(val division: TimeDivision) : HabitsAction

    data class UpdateTimeDivision(val division: TimeDivision) : HabitsAction

    data class DeleteTimeDivision(val id: Long) : HabitsAction

    data class ReorderTimeDivisions(val mapping: List<Pair<Int, TimeDivision>>) : HabitsAction

    data class SetHabitTimeDivision(val habitId: Long, val divisionId: Long?) : HabitsAction

    data class SelectTimeDivision(val divisionId: Long?) : HabitsAction

    data object ToggleTimeDivisionSheet : HabitsAction
}