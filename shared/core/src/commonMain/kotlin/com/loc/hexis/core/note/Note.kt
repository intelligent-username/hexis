package com.loc.hexis.core.note

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Note(
    val id: Long = 0,
    val title: String,
    val content: String = "",
    val type: NoteType = NoteType.MARKDOWN,
    val payloadJson: String? = null,
    val metadata: String? = null,
    val sortOrder: Int = 0,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val pinned: Boolean = false,
    val archived: Boolean = false,
) {
    fun parseCountingTable(): CountingTableData {
        val json = payloadJson
        if (type != NoteType.COUNTING_TABLE || json.isNullOrBlank()) return CountingTableData()
        return runCatching { Json.decodeFromString<CountingTableData>(json) }
            .getOrDefault(CountingTableData())
    }

    fun withCountingTable(data: CountingTableData): Note {
        return copy(
            type = NoteType.COUNTING_TABLE,
            payloadJson = Json.encodeToString(data),
        )
    }
}
