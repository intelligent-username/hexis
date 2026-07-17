package com.loc.hexis.core.tasks

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class PomodoroSession(
    val id: Long = 0,
    val goalDurationMinutes: Int,
    val timeStarted: LocalDateTime,
    val timeFinished: LocalDateTime? = null,
    val completed: Boolean = false,
    val timeCompletedMinutes: Float? = null,
    val linkedHabitId: Long? = null,
)
