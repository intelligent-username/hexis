package com.loc.hexis.core.note

import kotlinx.serialization.Serializable

@Serializable
data class CountingTableData(
    val rows: List<CounterRow> = emptyList(),
)
