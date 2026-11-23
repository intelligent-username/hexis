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

package com.loc.hexis.core.data

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.loc.hexis.core.interfaces.AlarmScheduler
import com.loc.hexis.core.interfaces.PomodoroAlarm
import com.loc.hexis.core.interfaces.SettingsDatastore
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class BootReceiver : BroadcastReceiver(), KoinComponent {
    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> Unit
            else -> return
        }
        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                if (
                    action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ) {
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (!alarmManager.canScheduleExactAlarms()) return@launch
                }

                try {
                    get<AlarmScheduler>().rescheduleAll()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to recover habit/task reminders", e)
                }

                try {
                    val session = get<SettingsDatastore>().getActivePomodoroSessionData().first()
                    if (
                        session != null &&
                            !session.isPaused &&
                            session.targetEndTimeMillis > Clock.System.now().toEpochMilliseconds()
                    ) {
                        get<PomodoroAlarm>().schedule(session.targetEndTimeMillis)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to recover Pomodoro alarm", e)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to recover alarms", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
