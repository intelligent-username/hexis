package com.loc.hexis.note.data.repository

import com.loc.hexis.core.note.Note
import com.loc.hexis.core.note.NoteRepo
import com.loc.hexis.note.data.database.NotesDao
import com.loc.hexis.note.data.toNote
import com.loc.hexis.note.data.toNoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [NoteRepo::class])
class NotesRepository(private val notesDao: NotesDao) : NoteRepo {

    override fun getNotesFlow(): Flow<List<Note>> {
        return notesDao
            .getNotesFlow()
            .map { entities -> entities.map { it.toNote() } }
            .flowOn(Dispatchers.IO)
    }

    override fun getArchivedNotesFlow(): Flow<List<Note>> {
        return notesDao
            .getArchivedNotesFlow()
            .map { entities -> entities.map { it.toNote() } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getNoteById(id: Long): Note? {
        return notesDao.getNoteById(id)?.toNote()
    }

    override suspend fun upsertNote(note: Note) {
        notesDao.upsertNote(note.toNoteEntity())
    }

    override suspend fun deleteNote(id: Long) {
        notesDao.deleteNote(id)
    }
}
