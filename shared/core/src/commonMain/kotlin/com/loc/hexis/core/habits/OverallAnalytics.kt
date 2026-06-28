package com.loc.hexis.core.habits

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

typealias HeatMapData = Map<LocalDate, Int>

@Serializable data class HabitRanking(val title: String, val consistency: Float)

@Serializable
data class OverallAnalytics(
    val heatMapData: HeatMapData = emptyMap(),
    val weekDayFrequencyData: WeekDayFrequencyData = emptyMap(),
    val completedHabits: Pair<LocalDate, List<String>>? = null,
    val consistency: Float = 0f,
    val topHabits: List<HabitRanking> = emptyList(),
)