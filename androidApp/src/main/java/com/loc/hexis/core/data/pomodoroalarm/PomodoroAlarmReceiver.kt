package com.loc.hexis.core.data.pomodoroalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.loc.hexis.core.data.notification.SilentHapticService

class PomodoroAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        SilentHapticService.start(context)
    }
}
