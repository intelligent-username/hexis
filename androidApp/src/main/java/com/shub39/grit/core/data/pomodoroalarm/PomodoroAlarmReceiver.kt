package com.shub39.grit.core.data.pomodoroalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shub39.grit.core.data.notification.SilentHapticService

class PomodoroAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        SilentHapticService.start(context)
    }
}
