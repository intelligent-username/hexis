package com.loc.hexis.note.data

import com.loc.hexis.core.note.Note
import com.loc.hexis.core.note.NoteType
import com.loc.hexis.note.data.database.NoteEntity

fun Note.toNoteEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        type = type.name,
        payloadJson = payloadJson,
        metadata = metadata,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
        pinned = pinned,
        archived = archived,
    )
}

fun NoteEntity.toNote(): Note {
    val noteType = runCatching { NoteType.valueOf(type) }.getOrDefault(NoteType.MARKDOWN)
    return Note(
        id = id,
        title = title,
        content = content,
        type = noteType,
        payloadJson = payloadJson,
        metadata = metadata,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
        pinned = pinned,
        archived = archived,
    )
}
