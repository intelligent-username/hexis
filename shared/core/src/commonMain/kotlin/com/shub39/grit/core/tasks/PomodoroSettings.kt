package com.shub39.grit.core.tasks

import kotlinx.serialization.Serializable

@Serializable
data class PomodoroSettings(
    val focusMinutes: Float = 25f,
    val shortBreakMinutes: Float = 5f,
    val longBreakMinutes: Float = 15f,
    val longBreakInterval: Int = 4,
)
