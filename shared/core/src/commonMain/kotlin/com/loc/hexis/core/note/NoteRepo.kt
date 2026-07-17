package com.loc.hexis.core.note

import kotlinx.coroutines.flow.Flow

interface NoteRepo {
    fun getNotesFlow(): Flow<List<Note>>

    fun getArchivedNotesFlow(): Flow<List<Note>>

    suspend fun getNoteById(id: Long): Note?

    suspend fun upsertNote(note: Note)

    suspend fun deleteNote(id: Long)
}
