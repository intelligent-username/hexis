package com.shub39.grit.core.tasks

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
)
