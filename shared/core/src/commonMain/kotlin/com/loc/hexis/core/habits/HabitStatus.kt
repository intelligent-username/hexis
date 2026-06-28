package com.loc.hexis.core.habits

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable data class HabitStatus(val id: Long = 0, val habitId: Long, val date: LocalDate, val value: Double = 0.0)