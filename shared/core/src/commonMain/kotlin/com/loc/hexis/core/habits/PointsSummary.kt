package com.loc.hexis.core.habits

data class PointsSummary(
    val currentWeekPoints: Int = 0,
    val lastWeekPoints: Int = 0,
    val totalPoints: Int = 0,
    val weeklyPointsHistory: List<Int> = emptyList(),
)
