package com.loc.hexis.core.habits

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class WeeklyPoints(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val pointsEarned: Int,
    val habitsCompleted: Int,
    val totalPossiblePoints: Int,
    val completionRate: Float,
    val streakBonusPoints: Int = 0,
    val perfectWeekBonusPoints: Int = 0,
)
