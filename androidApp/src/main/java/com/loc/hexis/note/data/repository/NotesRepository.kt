package com.loc.hexis.note.data.repository

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.loc.hexis.core.note.Note
import com.loc.hexis.core.note.NoteRepo
import com.loc.hexis.note.data.database.NotesDao
import com.loc.hexis.note.data.toNote
import com.loc.hexis.note.data.toNoteEntity
import com.loc.hexis.widgets.notes_shortcut_widget.NotesShortcutWidget
import com.loc.hexis.widgets.single_note_widget.SingleNoteWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [NoteRepo::class])
class NotesRepository(private val notesDao: NotesDao, private val context: Context) : NoteRepo {

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
        refreshWidgets()
    }

    override suspend fun updateSortOrders(orders: Map<Long, Int>) {
        notesDao.updateSortOrders(orders)
    }

    override suspend fun deleteNote(id: Long) {
        notesDao.deleteNote(id)
        refreshWidgets()
    }

    private suspend fun refreshWidgets() {
        try {
            val glanceManager = GlanceAppWidgetManager(context)

            val singleGlanceIds = glanceManager.getGlanceIds(SingleNoteWidget::class.java)
            for (id in singleGlanceIds) {
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[longPreferencesKey("last_updated")] = System.currentTimeMillis()
                    }
                }
                SingleNoteWidget().update(context, id)
            }

            val shortcutGlanceIds = glanceManager.getGlanceIds(NotesShortcutWidget::class.java)
            for (id in shortcutGlanceIds) {
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[longPreferencesKey("last_updated")] = System.currentTimeMillis()
                    }
                }
                NotesShortcutWidget().update(context, id)
            }
        } catch (_: Throwable) {}
    }
}
