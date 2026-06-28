package com.loc.hexis.core.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.loc.hexis.R
import com.loc.hexis.core.data.HexisIntentReceiver
import com.loc.hexis.core.habits.Habit
import com.loc.hexis.core.interfaces.IntentActions
import com.loc.hexis.core.tasks.Task
import org.koin.core.annotation.Single

@Single
class HexisNotificationManager(private val context: Context) {
    companion object {
        private const val TAG = "NotificationManager"
        private const val HABIT_NOTIF_ID_OFFSET = 0
        private const val TASK_NOTIF_ID_OFFSET = 1000

        fun createNotificationChannel(context: Context) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel("1", "Hexis Notifications", importance).apply {
                    description = "Notification Channel for Habits and Tasks"
                }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private val notificationManager by lazy { NotificationManagerCompat.from(context) }

    // shows habit notification if permission granted
    fun habitNotification(habit: Habit) {
        Log.d(TAG, "Sending Habit Notification")

        val intent =
            Intent(context, HexisIntentReceiver::class.java).apply {
                putExtra("habit_id", habit.id)
                action = IntentActions.ADD_HABIT_STATUS.action
            }
        val pendingBroadcast =
            PendingIntent.getBroadcast(
                context,
                habit.id.toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE,
            )

        val builder =
            NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.notif_icon)
                .setContentTitle(habit.title)
                .setContentText(habit.description)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .addAction(R.drawable.notif_icon, "Mark Done", pendingBroadcast)

        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(habit.id.toInt() + HABIT_NOTIF_ID_OFFSET, builder.build())
        } else {
            Log.e(TAG, "Notification permission denied!")
        }
    }

    // show task notification if permission granted
    fun taskNotification(task: Task) {
        val intent =
            Intent(context, HexisIntentReceiver::class.java).apply {
                putExtra("task_id", task.id)
                action = IntentActions.MARK_TASK_DONE.action
            }
        val pendingBroadcast =
            PendingIntent.getBroadcast(
                context,
                task.id.toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE,
            )
        val builder =
            NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.notif_icon)
                .setContentTitle(task.title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .addAction(R.drawable.notif_icon, "Mark Done", pendingBroadcast)

        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(task.id.toInt() + TASK_NOTIF_ID_OFFSET, builder.build())
        } else {
            Log.e(TAG, "Notification permission denied!")
        }
    }

    fun cancelNotification(habitId: Int) {
        notificationManager.cancel(habitId + HABIT_NOTIF_ID_OFFSET)
    }

    fun cancelNotification(task: Task) {
        notificationManager.cancel(task.id.toInt() + TASK_NOTIF_ID_OFFSET)
    }
}