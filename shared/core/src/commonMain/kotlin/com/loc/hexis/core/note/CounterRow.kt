package com.loc.hexis.core.note

import kotlinx.serialization.Serializable

@Serializable
data class CounterRow(
    val id: String,
    val label: String,
    val value: Double = 0.0,
    val step: Double = 1.0,
    val unit: String? = null,
    val isInteger: Boolean = true,
)
