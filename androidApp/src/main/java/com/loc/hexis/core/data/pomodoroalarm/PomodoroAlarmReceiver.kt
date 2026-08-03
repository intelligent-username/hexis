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
