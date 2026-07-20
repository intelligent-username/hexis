package com.loc.hexis.note.data

import com.loc.hexis.core.note.Note
import com.loc.hexis.note.data.database.NoteEntity

fun Note.toNoteEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        pinned = pinned,
        archived = archived,
    )
}

fun NoteEntity.toNote(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        pinned = pinned,
        archived = archived,
    )
}
