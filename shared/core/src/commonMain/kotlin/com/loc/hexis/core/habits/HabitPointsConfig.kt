package com.loc.hexis.core.habits

import kotlinx.serialization.Serializable

@Serializable
data class HabitPointsConfig(
    val habitId: Long,
    val basePoints: Int = 10,
    val streakBonusPerDay: Int = 2,
    val streakBonusCap: Int = 20,
    val perfectWeekBonus: Int = 50,
)
