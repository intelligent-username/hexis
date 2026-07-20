package com.loc.hexis.core.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.loc.hexis.core.data.HexisIntentReceiver
import com.loc.hexis.core.habits.Habit
import com.loc.hexis.core.interfaces.AlarmScheduler
import com.loc.hexis.core.interfaces.IntentActions
import com.loc.hexis.core.now
import com.loc.hexis.core.tasks.Task
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import org.koin.core.annotation.Single

// implementation of AlarmScheduler using AlarmManager
@Single(binds = [AlarmScheduler::class])
class NotificationAlarmScheduler(private val context: Context) : AlarmScheduler {

    companion object {
        private const val TAG = "NotificationAlarmScheduler"
    }

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: throw IllegalStateException("AlarmManager not available")

    override fun schedule(habit: Habit) {
        cancel(habit)
        if (!habit.reminder || habit.days.isEmpty()) return

        var scheduleTime = habit.time
        val now = LocalDateTime.Companion.now()

        while ((scheduleTime < now) || !habit.days.contains(scheduleTime.dayOfWeek)) {
            scheduleTime =
                scheduleTime.date.plus(1, DateTimeUnit.Companion.DAY).let {
                    LocalDateTime(date = it, time = scheduleTime.time)
                }
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

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            scheduleTime.toInstant(TimeZone.Companion.currentSystemDefault()).toEpochMilliseconds(),
            pendingIntent,
        )

        Log.d(TAG, "Scheduled habit #${habit.id} at $scheduleTime")
    }

    override fun schedule(task: Task) {
        cancel(task)
        if (task.reminder == null) return
        val scheduleTime = task.reminder!!

        val now = LocalDateTime.now()

        if (scheduleTime < now) {
            Log.d(TAG, "Task #${task.id} reminder time is in the past")
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

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            scheduleTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            pendingIntent,
        )

        Log.d(TAG, "Scheduled task #${task.id} at $scheduleTime")
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
