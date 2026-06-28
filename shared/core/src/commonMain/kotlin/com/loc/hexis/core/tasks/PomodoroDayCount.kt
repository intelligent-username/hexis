package com.loc.hexis.core.tasks

import kotlinx.datetime.LocalDate

data class PomodoroDayCount(
    val date: LocalDate,
    val count: Int,
)
