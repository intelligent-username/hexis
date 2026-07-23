package com.loc.hexis.core.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.loc.hexis.core.data.notification.HexisNotificationManager
import com.loc.hexis.core.habits.HabitRepo
import com.loc.hexis.core.habits.HabitStatus
import com.loc.hexis.core.interfaces.AlarmScheduler
import com.loc.hexis.core.interfaces.IntentActions
import com.loc.hexis.core.interfaces.SettingsDatastore
import com.loc.hexis.core.now
import com.loc.hexis.core.tasks.TaskRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

import com.loc.hexis.core.note.CountingTableData
import com.loc.hexis.core.note.NoteRepo

class HexisIntentReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val TAG = "HexisIntentReceiver"
    }

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                if (intent != null) {
                    when (intent.action) {
                        IntentActions.INCREMENT_NOTE_COUNTER.action -> incrementNoteCounter(intent)
                        IntentActions.DECREMENT_NOTE_COUNTER.action -> decrementNoteCounter(intent)
                        else -> {
                            val datastore = get<SettingsDatastore>()
                            val pauseNotifications = datastore.getNotificationsFlow().first()
                            if (!pauseNotifications) {
                                when (intent.action) {
                                    IntentActions.HABIT_NOTIFICATION.action -> habitNotification(intent)
                                    IntentActions.ADD_HABIT_STATUS.action -> addHabitStatus(intent)
                                    IntentActions.MARK_TASK_DONE.action -> markTaskDone(intent)
                                    IntentActions.TASK_NOTIFICATION.action -> taskNotification(intent)
                                    else -> return@launch
                                }
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Error: ", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun incrementNoteCounter(intent: Intent) {
        val noteId = intent.getLongExtra("note_id", -1L)
        val rowId = intent.getStringExtra("row_id") ?: return
        if (noteId < 0) return

        val noteRepo = get<NoteRepo>()
        val note = noteRepo.getNoteById(noteId) ?: return
        val tableData = note.parseCountingTable()
        val updatedRows = tableData.rows.map { r ->
            if (r.id == rowId) r.copy(value = r.value + r.step) else r
        }
        noteRepo.upsertNote(
            note.withCountingTable(CountingTableData(updatedRows))
                .copy(updatedAt = LocalDateTime.now())
        )
    }

    private suspend fun decrementNoteCounter(intent: Intent) {
        val noteId = intent.getLongExtra("note_id", -1L)
        val rowId = intent.getStringExtra("row_id") ?: return
        if (noteId < 0) return

        val noteRepo = get<NoteRepo>()
        val note = noteRepo.getNoteById(noteId) ?: return
        val tableData = note.parseCountingTable()
        val updatedRows = tableData.rows.map { r ->
            if (r.id == rowId) r.copy(value = (r.value - r.step).coerceAtLeast(0.0)) else r
        }
        noteRepo.upsertNote(
            note.withCountingTable(CountingTableData(updatedRows))
                .copy(updatedAt = LocalDateTime.now())
        )
    }

    private suspend fun markTaskDone(intent: Intent) {
        Log.d(TAG, "Mark task done intent received")
        val taskId = intent.getLongExtra("task_id", -1)
        if (taskId < 0) return

        val taskRepo = get<TaskRepo>()
        val task = taskRepo.getTaskById(taskId) ?: return

        taskRepo.upsertTask(task.copy(status = true, reminder = null))

        Log.d(TAG, "Task marked as complete successfully")

        get<HexisNotificationManager>().cancelNotification(taskId.toInt())
    }

    private suspend fun addHabitStatus(intent: Intent) {
        Log.d(TAG, "Add habit status intent received")
        val habitId = intent.getLongExtra("habit_id", -1)
        if (habitId < 0) return

        val habitRepo = get<HabitRepo>()
        val habit = habitRepo.getHabitById(habitId) ?: return

        habitRepo.insertHabitStatus(
            HabitStatus(habitId = habitId, date = LocalDate.now(), value = habit.targetValue ?: 1.0)
        )

        Log.d(TAG, "Habit status added successfully")

        get<HexisNotificationManager>().cancelNotification(habitId.toInt())
    }

    private suspend fun taskNotification(intent: Intent) {
        Log.d(TAG, "Task notification intent received")
        val taskId = intent.getLongExtra("task_id", -1)
        if (taskId < 0) return

        val taskRepo = get<TaskRepo>()

        val task = taskRepo.getTaskById(taskId) ?: return
        if (!task.status && task.reminder != null) {
            Log.d(TAG, "sending Task notification")
            get<HexisNotificationManager>().taskNotification(task)
        }
    }

    private suspend fun habitNotification(intent: Intent) {
        Log.d(TAG, "Habit notification intent received")

        val habitId = intent.getLongExtra("habit_id", -1)
        if (habitId < 0L) return

        val habitRepo = get<HabitRepo>()

        val habit = habitRepo.getHabitById(habitId) ?: return
        if (!habit.reminder) return

        // check if habit is completed today, if not then show notification
        val habitStatus = habitRepo.getStatusForHabit(habitId)
        if (habitStatus.any { it.date == LocalDate.now() }) {
            Log.d(TAG, "Habit already completed today")
        } else {
            get<HexisNotificationManager>().habitNotification(habit)
        }

        get<AlarmScheduler>().schedule(habit)
    }
}
