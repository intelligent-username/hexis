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

    fun parseJournal(): JournalNoteData {
        val json = payloadJson
        if (type != NoteType.JOURNAL || json.isNullOrBlank()) return JournalNoteData()
        return runCatching { Json.decodeFromString<JournalNoteData>(json) }
            .getOrDefault(JournalNoteData())
    }

    fun withJournal(data: JournalNoteData): Note {
        return copy(
            type = NoteType.JOURNAL,
            payloadJson = Json.encodeToString(data),
        )
    }

    fun parseVault(): VaultNote {
        val json = payloadJson
        if (type != NoteType.VAULT || json.isNullOrBlank()) return VaultNote()
        return runCatching { Json.decodeFromString<VaultNote>(json) }
            .getOrDefault(VaultNote())
    }

    fun withVault(data: VaultNote): Note {
        return copy(
            type = NoteType.VAULT,
            payloadJson = Json.encodeToString(data),
        )
    }

    fun getColorHex(): String? {
        val meta = metadata
        if (meta.isNullOrBlank()) return null
        return runCatching {
            val jsonObj = kotlinx.serialization.json.Json.parseToJsonElement(meta).let {
                if (it is kotlinx.serialization.json.JsonObject) it else null
            }
            jsonObj?.get("colorHex")?.let {
                if (it is kotlinx.serialization.json.JsonPrimitive) it.content else null
            }
        }.getOrNull() ?: if (meta.startsWith("#") || meta.startsWith("preset:")) meta else null
    }

    fun withColorHex(colorHex: String?): Note {
        val meta = metadata
        val currentObj = runCatching {
            if (!meta.isNullOrBlank()) {
                kotlinx.serialization.json.Json.parseToJsonElement(meta).let {
                    if (it is kotlinx.serialization.json.JsonObject) it else null
                }
            } else null
        }.getOrNull()

        val newObj = kotlinx.serialization.json.buildJsonObject {
            currentObj?.forEach { (key, value) ->
                if (key != "colorHex") {
                    put(key, value)
                }
            }
            if (colorHex != null) {
                put("colorHex", kotlinx.serialization.json.JsonPrimitive(colorHex))
            }
        }
        return copy(metadata = newObj.toString())
    }
}
