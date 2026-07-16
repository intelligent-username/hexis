/*
 * Copyright (C) 2025-2026 Hexis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
