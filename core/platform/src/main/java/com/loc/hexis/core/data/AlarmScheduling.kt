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
import android.app.PendingIntent
import android.os.Build
import android.util.Log

internal fun AlarmManager.scheduleRtcWakeup(
    timeMillis: Long,
    pendingIntent: PendingIntent,
    allowWhileIdle: Boolean = true,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms()) {
        setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
        return
    }

    try {
        if (allowWhileIdle) {
            setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
        } else {
            setExact(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
        }
    } catch (e: SecurityException) {
        Log.w("AlarmScheduling", "Exact alarm access denied; scheduling an inexact alarm", e)
        setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
    }
}
