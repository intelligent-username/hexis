package com.loc.hexis.core.data.pomodoroalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.loc.hexis.core.data.notification.SilentHapticService
import com.loc.hexis.shared.ui.task.PomodoroManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class PomodoroAlarmReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent?) {
        SilentHapticService.start(context)
        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val pomodoroManager = get<PomodoroManager>()
                pomodoroManager.onAlarmFired()
            } catch (t: Throwable) {
                Log.e("PomodoroAlarmReceiver", "Error finalizing alarm session", t)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
