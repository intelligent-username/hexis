package com.shub39.grit.core.interfaces

interface PomodoroAlarm {
    fun schedule(timeMillis: Long)
    fun cancel()
}
