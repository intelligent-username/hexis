package com.loc.hexis.note.data.database

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes WHERE archived = 0 ORDER BY pinned DESC, sortOrder ASC, createdAt DESC")
    fun getNotesFlow(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE archived = 1 ORDER BY sortOrder ASC, createdAt DESC")
    fun getArchivedNotesFlow(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id") suspend fun getNoteById(id: Long): NoteEntity?

    @Upsert suspend fun upsertNote(note: NoteEntity): Long

    @Query("UPDATE notes SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    @Transaction
    suspend fun updateSortOrders(orders: Map<Long, Int>) {
        orders.forEach { (id, order) -> updateSortOrder(id, order) }
    }

    @Query("DELETE FROM notes WHERE id = :id") suspend fun deleteNote(id: Long)
}
