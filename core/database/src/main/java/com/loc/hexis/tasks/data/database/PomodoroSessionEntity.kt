
package com.loc.hexis.tasks.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalDurationMinutes: Int,
    val timeStarted: LocalDateTime,
    val timeFinished: LocalDateTime? = null,
    val completed: Boolean = false,
    val timeCompletedMinutes: Float? = null,
    val linkedHabitId: Long? = null,
)
