package com.loc.hexis.core.note

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class JournalNoteData(
    val entries: List<JournalEntry> = emptyList()
)

@Serializable
data class JournalEntry(
    val id: String,
    val timestamp: LocalDateTime,
    val text: String,
    val mood: String? = null
)
