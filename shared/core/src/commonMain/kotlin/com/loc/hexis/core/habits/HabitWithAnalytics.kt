package com.loc.hexis.core.habits

typealias WeeklyComparisonData = List<Double>

typealias WeekDayFrequencyData = Map<String, Int>

/** Grouped model for [habit] and its constituent analytics calculated from [statuses] */
data class HabitWithAnalytics(
    val habit: Habit,
    val consistency: Float,
    val statuses: List<HabitStatus>,
    val weeklyComparisonData: WeeklyComparisonData,
    val weekDayFrequencyData: WeekDayFrequencyData,
    val currentStreak: Int,
    val bestStreak: Int,
    val startedDaysAgo: Long,
    val pointsSummary: PointsSummary = PointsSummary(),
)
