package com.shub39.grit.core.tasks

import kotlinx.datetime.LocalDate

data class PomodoroDayCount(
    val date: LocalDate,
    val count: Int,
)
