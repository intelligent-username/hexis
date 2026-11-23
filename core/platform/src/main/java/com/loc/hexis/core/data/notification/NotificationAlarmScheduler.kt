/*
 * Copyright (C) 2025-2026 Hexis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.loc.hexis.core.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.loc.hexis.core.data.HexisIntentReceiver
import com.loc.hexis.core.data.scheduleRtcWakeup
import com.loc.hexis.core.habits.Habit
import com.loc.hexis.core.habits.HabitRepo
import com.loc.hexis.core.interfaces.AlarmScheduler
import com.loc.hexis.core.interfaces.IntentActions
import com.loc.hexis.core.interfaces.SettingsDatastore
import com.loc.hexis.core.tasks.Task
import com.loc.hexis.core.tasks.TaskRepo
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

// implementation of AlarmScheduler using AlarmManager
@Single(binds = [AlarmScheduler::class])
class NotificationAlarmScheduler(
    private val context: Context,
    @Provided private val habitRepo: HabitRepo,
    @Provided private val taskRepo: TaskRepo,
    private val settingsDatastore: SettingsDatastore,
) : AlarmScheduler {

    companion object {
        private const val TAG = "NotificationAlarmScheduler"
    }

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: throw IllegalStateException("AlarmManager not available")

    override fun schedule(habit: Habit) {
        cancel(habit)
        if (!habit.reminder || habit.days.isEmpty()) return

        val timeZone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        var scheduleTime =
            LocalDateTime(
                date = maxOf(now.toLocalDateTime(timeZone).date, habit.time.date),
                time = habit.time.time,
            )

        while (
            scheduleTime.toInstant(timeZone) <= now ||
                scheduleTime.dayOfWeek !in habit.days
        ) {
            scheduleTime =
                LocalDateTime(
                    date = scheduleTime.date.plus(1, DateTimeUnit.DAY),
                    time = habit.time.time,
                )
        }

        val notificationIntent =
            Intent(context, HexisIntentReceiver::class.java).apply {
                action = IntentActions.HABIT_NOTIFICATION.action
                putExtra("habit_id", habit.id)
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                habit.id.toInt(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        alarmManager.scheduleRtcWakeup(
            scheduleTime.toInstant(timeZone).toEpochMilliseconds(),
            pendingIntent,
        )

        Log.d(TAG, "Scheduled habit #${habit.id} at $scheduleTime")
    }

    override fun schedule(task: Task) {
        cancel(task)
        if (task.status) return
        val scheduleTime = task.reminder ?: return

        val scheduleInstant = scheduleTime.toInstant(TimeZone.currentSystemDefault())
        if (scheduleInstant <= Clock.System.now()) {
            Log.d(TAG, "Task #${task.id} reminder time is not in the future")
            return
        }

        val notificationIntent =
            Intent(context, HexisIntentReceiver::class.java).apply {
                action = IntentActions.TASK_NOTIFICATION.action
                putExtra("task_id", task.id)
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                task.id.toInt(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        alarmManager.scheduleRtcWakeup(
            scheduleInstant.toEpochMilliseconds(),
            pendingIntent,
        )

        Log.d(TAG, "Scheduled task #${task.id} at $scheduleTime")
    }

    override suspend fun rescheduleAll() {
        try {
            val archivedIds = settingsDatastore.getArchivedHabitIds().first()
            habitRepo.getHabits().forEach { habit ->
                try {
                    if (habit.id in archivedIds || !habit.reminder || habit.days.isEmpty()) {
                        cancel(habit)
                    } else {
                        schedule(habit)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reconcile habit #${habit.id}", e)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read habit reminder sources", e)
        }

        try {
            taskRepo.getTasks().forEach { task ->
                try {
                    schedule(task)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reconcile task #${task.id}", e)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read task reminder source", e)
        }
    }

    override fun cancel(habit: Habit) {
        val cancelIntent =
            Intent(context, HexisIntentReceiver::class.java).apply {
                action = IntentActions.HABIT_NOTIFICATION.action
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                habit.id.toInt(),
                cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled habit #${habit.id}")
    }

    override fun cancel(task: Task) {
        val cancelIntent =
            Intent(context, HexisIntentReceiver::class.java).apply {
                action = IntentActions.TASK_NOTIFICATION.action
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                task.id.toInt(),
                cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled task #${task.id}")
    }

    override fun cancelAll() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            alarmManager.cancelAll()
        }
    }
}
