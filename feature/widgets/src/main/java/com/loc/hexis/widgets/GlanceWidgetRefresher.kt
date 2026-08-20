package com.loc.hexis.widgets

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.loc.hexis.core.interfaces.WidgetRefresher
import com.loc.hexis.widgets.habit_overview_widget.HabitOverviewWidget
import com.loc.hexis.widgets.habit_streak_widget.HabitStreakWidget
import com.loc.hexis.widgets.habit_week_chart_widget.HabitWeekChartWidget
import com.loc.hexis.widgets.notes_shortcut_widget.NotesShortcutWidget
import com.loc.hexis.widgets.progress_widget.ProgressWidget
import com.loc.hexis.widgets.single_note_widget.SingleNoteWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [WidgetRefresher::class])
class GlanceWidgetRefresher(@Provided private val context: Context) : WidgetRefresher {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun refreshHabitWidgets() {
        scope.launch {
            runCatching {
                ProgressWidget().updateAll(context)
                HabitOverviewWidget().updateAll(context)
                HabitStreakWidget().updateAll(context)
                HabitWeekChartWidget().updateAll(context)
            }
        }
    }

    override fun refreshNoteWidgets() {
        scope.launch {
            runCatching {
                NotesShortcutWidget().updateAll(context)
                SingleNoteWidget().updateAll(context)
            }
        }
    }
}
