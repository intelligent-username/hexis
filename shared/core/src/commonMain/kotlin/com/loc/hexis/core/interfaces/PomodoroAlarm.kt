package com.loc.hexis.core.interfaces

interface PomodoroAlarm {
    fun schedule(timeMillis: Long)
    fun cancel()
}
