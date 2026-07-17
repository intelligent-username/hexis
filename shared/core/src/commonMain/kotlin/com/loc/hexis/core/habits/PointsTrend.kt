package com.loc.hexis.core.habits

import kotlinx.serialization.Serializable

@Serializable
data class PointsTrend(
    val weeklyPoints: List<WeeklyPoints>,
    val currentWeekPoints: Int,
    val previousWeekPoints: Int,
    val netChange: Int,
    val netChangePercent: Float,
    val totalPointsAllTime: Int,
    val averageWeeklyPoints: Float,
    val bestWeekPoints: Int,
    val currentStreakWeeks: Int,
    val currentPartialPoints: Int = 0,
    val previousPartialPoints: Int = 0,
    val partialNetChange: Int = 0,
    val partialNetChangePercent: Float = 0f,
) {
    companion object {
        val empty =
            PointsTrend(
                weeklyPoints = emptyList(),
                currentWeekPoints = 0,
                previousWeekPoints = 0,
                netChange = 0,
                netChangePercent = 0f,
                totalPointsAllTime = 0,
                averageWeeklyPoints = 0f,
                bestWeekPoints = 0,
                currentStreakWeeks = 0,
            )
    }
}
