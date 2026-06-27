package com.shub39.grit.core.habits

import kotlinx.serialization.Serializable

@Serializable
data class TimeDivision(
    val id: Long,
    val name: String,
    val index: Int,
)
