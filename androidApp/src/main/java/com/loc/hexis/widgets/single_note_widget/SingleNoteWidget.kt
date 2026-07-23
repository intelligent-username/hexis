package com.loc.hexis.widgets.single_note_widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.currentState
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.loc.hexis.app.MainActivity
import com.loc.hexis.core.interfaces.ThemeDatastore
import com.loc.hexis.core.interfaces.WidgetActions
import com.loc.hexis.core.note.CounterRow
import com.loc.hexis.core.note.CountingTableData
import com.loc.hexis.core.note.Note
import com.loc.hexis.core.note.NoteRepo
import com.loc.hexis.core.note.NoteType
import com.loc.hexis.core.now
import com.loc.hexis.shared.ui.note.LineType
import com.loc.hexis.shared.ui.note.parseContentLines
import com.loc.hexis.shared.ui.note.getNoteColor
import com.loc.hexis.widgets.rememberWidgetColorProviders
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

val noteIdPrefKey = longPreferencesKey("single_note_id")

private data class NoteCardColors(
    val background: ColorProvider,
    val onSurface: ColorProvider,
    val onSurfaceVariant: ColorProvider,
    val primary: ColorProvider,
)

@Composable
private fun getNoteCardColors(note: Note?, isDark: Boolean): NoteCardColors {
    if (note == null) {
        return NoteCardColors(
            background = GlanceTheme.colors.surface,
            onSurface = GlanceTheme.colors.onSurface,
            onSurfaceVariant = GlanceTheme.colors.onSurfaceVariant,
            primary = GlanceTheme.colors.primary,
        )
    }
    val customColor = getNoteColor(note.getColorHex(), isDark)
    return if (customColor != Color.Unspecified) {
        val isDarkBg = customColor.luminance() < 0.5f
        val textColor = if (isDarkBg) Color.White else Color.Black
        val variantColor = if (isDarkBg) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
        NoteCardColors(
            background = ColorProvider(customColor),
            onSurface = ColorProvider(textColor),
            onSurfaceVariant = ColorProvider(variantColor),
            primary = ColorProvider(textColor),
        )
    } else {
        NoteCardColors(
            background = GlanceTheme.colors.surface,
            onSurface = GlanceTheme.colors.onSurface,
            onSurfaceVariant = GlanceTheme.colors.onSurfaceVariant,
            primary = GlanceTheme.colors.primary,
        )
    }
}

class SingleNoteWidget : GlanceAppWidget(), KoinComponent {

    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = get<NoteRepo>()
        val themeDatastore = get<ThemeDatastore>()

        provideContent {
            val scope = rememberCoroutineScope()
            val size = LocalSize.current
            val state = currentState<Preferences>()
            val storedNoteId = state[noteIdPrefKey]

            val allNotes by repo.getNotesFlow().collectAsState(emptyList())
            val targetNote = allNotes.find { it.id == storedNoteId }

            val appTheme by themeDatastore.getAppThemeFlow().collectAsState(com.loc.hexis.core.theme.AppTheme.SYSTEM)
            val seedColor by themeDatastore.getSeedColorFlow().collectAsState(0xFFFFFF)
            val isAmoled by themeDatastore.getAmoledPref().collectAsState(false)
            val paletteStyle by themeDatastore.getPaletteStyle().collectAsState(com.loc.hexis.core.theme.PaletteStyle.TONALSPOT)
            val isMaterialYou by themeDatastore.getMaterialYouFlow().collectAsState(false)

            val isDark = appTheme == com.loc.hexis.core.theme.AppTheme.DARK || isAmoled

            val colors = rememberWidgetColorProviders(
                appTheme = appTheme,
                seedColor = seedColor,
                isAmoled = isAmoled,
                paletteStyle = paletteStyle,
                isMaterialYou = isMaterialYou,
            )

            key(size, storedNoteId, targetNote?.payloadJson, targetNote?.updatedAt) {
                GlanceTheme(colors = colors) {
                    SingleNoteContent(
                        note = targetNote,
                        isDark = isDark,
                        onCounterAction = { noteId, rowId, isIncrement ->
                            scope.launch {
                                val note = repo.getNoteById(noteId) ?: return@launch
                                val tableData = note.parseCountingTable()
                                val updatedRows = tableData.rows.map { r ->
                                    if (r.id == rowId) {
                                        if (isIncrement) r.copy(value = r.value + r.step)
                                        else r.copy(value = (r.value - r.step).coerceAtLeast(0.0))
                                    } else r
                                }
                                repo.upsertNote(
                                    note.withCountingTable(CountingTableData(updatedRows))
                                        .copy(updatedAt = LocalDateTime.now())
                                )
                            }
                        },
                    )
                }
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val now = LocalDateTime.now()
        val sampleNote = Note(
            id = 1,
            title = "Daily Reflections",
            content = "1. Worked on KMP architecture\n2. Polished UI components\n3. Built Glance widgets",
            type = NoteType.MARKDOWN,
            createdAt = now,
            updatedAt = now,
        )

        provideContent {
            val colors = rememberWidgetColorProviders(
                appTheme = com.loc.hexis.core.theme.AppTheme.LIGHT,
                seedColor = 0xFFFFFF,
                isAmoled = false,
                paletteStyle = com.loc.hexis.core.theme.PaletteStyle.TONALSPOT,
                isMaterialYou = false,
            )
            GlanceTheme(colors = colors) {
                SingleNoteContent(note = sampleNote, isDark = false, onCounterAction = { _, _, _ -> })
            }
        }
    }
}

@GlanceComposable
@Composable
private fun SingleNoteContent(
    note: Note?,
    isDark: Boolean,
    onCounterAction: (noteId: Long, rowId: String, isIncrement: Boolean) -> Unit,
) {
    val context = LocalContext.current
    val openAction = actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
            if (note != null) {
                putExtra("shortcut_action", WidgetActions.OPEN_NOTE)
                putExtra("note_id", note.id)
            } else {
                putExtra("shortcut_action", WidgetActions.OPEN_NOTES)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    )

    val cardColors = getNoteCardColors(note, isDark)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(12.dp),
    ) {
        if (note == null) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(openAction),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tap to configure note widget",
                        style = TextStyle(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }
            }
        } else if (note.type == NoteType.VAULT) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(openAction),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🔒 Vault Protected",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onSurface,
                        ),
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "Vault notes cannot be previewed on widgets for security.",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = GlanceTheme.colors.outline,
                        ),
                    )
                }
            }
        } else {
            // Widget Header
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = note.title.ifBlank { "Untitled Note" },
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = cardColors.onSurface,
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.fillMaxWidth().clickable(openAction),
                )
            }

            // Note Type Tailored Content
            when (note.type) {
                NoteType.MARKDOWN -> {
                    val lines = note.content.let {
                        if (it.isBlank()) emptyList()
                        else parseContentLines(it)
                    }
                    if (lines.isEmpty()) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .cornerRadius(10.dp)
                                .background(cardColors.background)
                                .padding(10.dp)
                                .clickable(openAction),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No content",
                                style = TextStyle(fontSize = 12.sp, color = cardColors.onSurfaceVariant),
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .cornerRadius(10.dp)
                                .background(cardColors.background)
                                .padding(10.dp),
                        ) {
                            items(lines) { line ->
                                when (line.type) {
                                    LineType.HEADER -> Text(
                                        text = line.text,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = cardColors.onSurface,
                                        ),
                                        modifier = GlanceModifier.clickable(openAction),
                                    )
                                    LineType.SUB_HEADER -> Text(
                                        text = line.text,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = cardColors.onSurface,
                                        ),
                                        modifier = GlanceModifier.clickable(openAction),
                                    )
                                    LineType.SUB_SUB_HEADER -> Text(
                                        text = line.text,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = cardColors.onSurface,
                                        ),
                                        modifier = GlanceModifier.clickable(openAction),
                                    )
                                    LineType.BULLET_LIST -> Text(
                                        text = "• ${line.text}",
                                        style = TextStyle(fontSize = 12.sp, color = cardColors.onSurfaceVariant),
                                        modifier = GlanceModifier.clickable(openAction),
                                    )
                                    LineType.NUMBERED_LIST -> Text(
                                        text = "${line.number ?: 1}. ${line.text}",
                                        style = TextStyle(fontSize = 12.sp, color = cardColors.onSurfaceVariant),
                                        modifier = GlanceModifier.clickable(openAction),
                                    )
                                    LineType.CHECKLIST -> Text(
                                        text = "${if (line.isChecked) "☑" else "☐"} ${line.text}",
                                        style = TextStyle(fontSize = 12.sp, color = cardColors.onSurfaceVariant),
                                        modifier = GlanceModifier.clickable(openAction),
                                    )
                                    LineType.QUOTE -> Text(
                                        text = "│ ${line.text}",
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            color = cardColors.primary,
                                        ),
                                        modifier = GlanceModifier.clickable(openAction),
                                    )
                                    LineType.HORIZONTAL_RULE -> Box(
                                        modifier = GlanceModifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(GlanceTheme.colors.outline),
                                    ) {}
                                    LineType.REGULAR -> Text(
                                        text = line.text,
                                        style = TextStyle(fontSize = 12.sp, color = cardColors.onSurfaceVariant),
                                        modifier = GlanceModifier.clickable(openAction),
                                    )
                                }
                                Spacer(modifier = GlanceModifier.height(3.dp))
                            }
                        }
                    }
                }

                NoteType.COUNTING_TABLE -> {
                    val tableData = note.parseCountingTable()
                    if (tableData.rows.isEmpty()) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .cornerRadius(10.dp)
                                .background(cardColors.background)
                                .padding(10.dp)
                                .clickable(openAction),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No counting rows",
                                style = TextStyle(fontSize = 12.sp, color = cardColors.onSurfaceVariant),
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = GlanceModifier.fillMaxSize(),
                        ) {
                            items(tableData.rows, itemId = { row -> (row.id + "_" + row.value).hashCode().toLong() }) { row ->
                                CounterWidgetRow(
                                    noteId = note.id,
                                    row = row,
                                    cardColors = cardColors,
                                    openAction = openAction,
                                    onCounterAction = onCounterAction,
                                )
                                Spacer(modifier = GlanceModifier.height(4.dp))
                            }
                        }
                    }
                }

                NoteType.JOURNAL -> {
                    val journalData = note.parseJournal()
                    if (journalData.entries.isEmpty()) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .cornerRadius(10.dp)
                                .background(cardColors.background)
                                .padding(10.dp)
                                .clickable(openAction),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No journal entries",
                                style = TextStyle(fontSize = 12.sp, color = cardColors.onSurfaceVariant),
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = GlanceModifier.fillMaxSize(),
                        ) {
                            items(journalData.entries) { entry ->
                                Box(
                                    modifier = GlanceModifier
                                        .fillMaxWidth()
                                        .cornerRadius(8.dp)
                                        .background(cardColors.background)
                                        .padding(8.dp)
                                        .clickable(openAction),
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (!entry.mood.isNullOrBlank()) {
                                                Text(
                                                    text = entry.mood!!,
                                                    style = TextStyle(fontSize = 12.sp),
                                                    modifier = GlanceModifier.padding(end = 4.dp),
                                                )
                                            }
                                            Text(
                                                text = entry.timestamp.date.toString(),
                                                style = TextStyle(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = cardColors.primary,
                                                ),
                                            )
                                        }
                                        Spacer(modifier = GlanceModifier.height(2.dp))
                                        Text(
                                            text = entry.text,
                                            style = TextStyle(
                                                fontSize = 11.sp,
                                                color = cardColors.onSurfaceVariant,
                                            ),
                                        )
                                    }
                                }
                                Spacer(modifier = GlanceModifier.height(4.dp))
                            }
                        }
                    }
                }

                NoteType.VAULT -> {}
            }
        }
    }
}

@GlanceComposable
@Composable
private fun CounterWidgetRow(
    noteId: Long,
    row: CounterRow,
    cardColors: NoteCardColors,
    openAction: androidx.glance.action.Action,
    onCounterAction: (noteId: Long, rowId: String, isIncrement: Boolean) -> Unit,
) {
    val valText = if (row.value % 1.0 == 0.0) row.value.toLong().toString() else row.value.toString()

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(10.dp)
            .background(cardColors.background)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = row.label.ifBlank { "Row" },
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = cardColors.onSurface,
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight().clickable(openAction),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Minus Button
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(6.dp)
                        .background(GlanceTheme.colors.secondaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .clickable { onCounterAction(noteId, row.id, false) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "−",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onSecondaryContainer,
                        ),
                    )
                }

                // Value Display
                Box(
                    modifier = GlanceModifier.padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = valText,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = cardColors.primary,
                        ),
                    )
                }

                // Plus Button
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(6.dp)
                        .background(GlanceTheme.colors.primary)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .clickable { onCounterAction(noteId, row.id, true) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onPrimary,
                        ),
                    )
                }
            }
        }
    }
}
