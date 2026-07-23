package com.loc.hexis.widgets.notes_shortcut_widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.loc.hexis.R
import com.loc.hexis.app.MainActivity
import com.loc.hexis.core.data.HexisIntentReceiver
import com.loc.hexis.core.interfaces.IntentActions
import com.loc.hexis.core.interfaces.ThemeDatastore
import com.loc.hexis.core.interfaces.WidgetActions
import com.loc.hexis.core.note.Note
import com.loc.hexis.core.note.NoteRepo
import com.loc.hexis.core.note.NoteType
import com.loc.hexis.core.now
import com.loc.hexis.shared.ui.note.getContentPreview
import com.loc.hexis.shared.ui.note.getNoteColor
import com.loc.hexis.widgets.rememberWidgetColorProviders
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

private data class NoteCardColors(
    val background: ColorProvider,
    val onSurface: ColorProvider,
    val onSurfaceVariant: ColorProvider,
    val primary: ColorProvider,
)

@Composable
private fun getNoteCardColors(note: Note, isDark: Boolean): NoteCardColors {
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

class NotesShortcutWidget : GlanceAppWidget(), KoinComponent {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = get<NoteRepo>()
        val themeDatastore = get<ThemeDatastore>()

        val notes = repo.getNotesFlow().first()

        val appTheme = themeDatastore.getAppThemeFlow().first()
        val seedColor = themeDatastore.getSeedColorFlow().first()
        val isAmoled = themeDatastore.getAmoledPref().first()
        val paletteStyle = themeDatastore.getPaletteStyle().first()
        val isMaterialYou = themeDatastore.getMaterialYouFlow().first()

        val isDark = appTheme == com.loc.hexis.core.theme.AppTheme.DARK || isAmoled

        provideContent {
            val size = LocalSize.current
            val colors = rememberWidgetColorProviders(
                appTheme = appTheme,
                seedColor = seedColor,
                isAmoled = isAmoled,
                paletteStyle = paletteStyle,
                isMaterialYou = isMaterialYou,
            )

            key(size, notes.hashCode()) {
                GlanceTheme(colors = colors) {
                    NotesWidgetContent(notes = notes, isDark = isDark)
                }
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val now = LocalDateTime.now()
        val sampleNotes = listOf(
            Note(
                id = 1,
                title = "Shopping List",
                content = "# Groceries\n- Apples\n- Almond Milk\n- Whole Wheat Bread",
                type = NoteType.MARKDOWN,
                createdAt = now,
                updatedAt = now,
            ),
            Note(
                id = 2,
                title = "Daily Workout Tracker",
                type = NoteType.COUNTING_TABLE,
                createdAt = now,
                updatedAt = now,
            ),
            Note(
                id = 3,
                title = "Private Vault",
                type = NoteType.VAULT,
                createdAt = now,
                updatedAt = now,
            ),
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
                NotesWidgetContent(notes = sampleNotes, isDark = false)
            }
        }
    }
}

@GlanceComposable
@Composable
private fun NotesWidgetContent(notes: List<Note>, isDark: Boolean) {
    val context = LocalContext.current
    val size = LocalSize.current
    val openNotesAction = actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
            putExtra("shortcut_action", WidgetActions.OPEN_NOTES)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    )

    val useTwoColumns = size.width >= 220.dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(openNotesAction)
            .padding(12.dp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_shortcut_notes),
                contentDescription = "Notes",
                modifier = GlanceModifier.padding(end = 6.dp),
            )
            Text(
                text = "Notes",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = GlanceTheme.colors.onSurface,
                ),
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "${notes.size} ${if (notes.size == 1) "note" else "notes"}",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.outline,
                ),
            )
        }

        if (notes.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No notes yet",
                        style = TextStyle(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = "Tap to open Notes tab",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = GlanceTheme.colors.outline,
                        ),
                    )
                }
            }
        } else if (useTwoColumns) {
            val col1Notes = notes.filterIndexed { index, _ -> index % 2 == 0 }
            val col2Notes = notes.filterIndexed { index, _ -> index % 2 == 1 }

            Row(modifier = GlanceModifier.fillMaxSize()) {
                LazyColumn(modifier = GlanceModifier.defaultWeight()) {
                    items(col1Notes) { note ->
                        NoteCardItem(note = note, isDark = isDark)
                        Spacer(modifier = GlanceModifier.height(6.dp))
                    }
                }
                Spacer(modifier = GlanceModifier.width(8.dp))
                LazyColumn(modifier = GlanceModifier.defaultWeight()) {
                    items(col2Notes) { note ->
                        NoteCardItem(note = note, isDark = isDark)
                        Spacer(modifier = GlanceModifier.height(6.dp))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = GlanceModifier.fillMaxSize(),
            ) {
                items(notes) { note ->
                    NoteCardItem(note = note, isDark = isDark)
                    Spacer(modifier = GlanceModifier.height(6.dp))
                }
            }
        }
    }
}

@GlanceComposable
@Composable
private fun NoteCardItem(note: Note, isDark: Boolean) {
    val context = LocalContext.current
    val titleText = note.title.ifBlank { "Untitled Note" }
    val openNoteAction = actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
            putExtra("shortcut_action", WidgetActions.OPEN_NOTE)
            putExtra("note_id", note.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    )

    val cardColors = getNoteCardColors(note, isDark)

    when (note.type) {
        NoteType.COUNTING_TABLE -> {
            val tableData = note.parseCountingTable()

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .cornerRadius(12.dp)
                    .background(cardColors.background)
                    .padding(10.dp),
            ) {
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = titleText,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = cardColors.onSurface,
                            ),
                            maxLines = 1,
                            modifier = GlanceModifier.defaultWeight().clickable(openNoteAction),
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    if (tableData.rows.isEmpty()) {
                        Text(
                            text = "No counters added yet",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = cardColors.onSurfaceVariant,
                            ),
                            modifier = GlanceModifier.clickable(openNoteAction),
                        )
                    } else {
                        tableData.rows.take(3).forEachIndexed { index, row ->
                            val valText = if (row.value % 1.0 == 0.0) row.value.toLong().toString() else row.value.toString()
                            val labelText = row.label.ifBlank { "Item ${index + 1}" }

                            val decrementIntent = Intent(context, HexisIntentReceiver::class.java).apply {
                                action = IntentActions.DECREMENT_NOTE_COUNTER.action
                                putExtra("note_id", note.id)
                                putExtra("row_id", row.id)
                            }

                            val incrementIntent = Intent(context, HexisIntentReceiver::class.java).apply {
                                action = IntentActions.INCREMENT_NOTE_COUNTER.action
                                putExtra("note_id", note.id)
                                putExtra("row_id", row.id)
                            }

                            if (index > 0) {
                                Spacer(modifier = GlanceModifier.height(3.dp))
                            }

                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = labelText,
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        color = cardColors.onSurfaceVariant,
                                    ),
                                    maxLines = 1,
                                    modifier = GlanceModifier.defaultWeight().clickable(openNoteAction),
                                )

                                Spacer(modifier = GlanceModifier.width(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Minus Button
                                    Box(
                                        modifier = GlanceModifier
                                            .cornerRadius(6.dp)
                                            .background(GlanceTheme.colors.secondaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                            .clickable(actionSendBroadcast(decrementIntent)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "−",
                                            style = TextStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = GlanceTheme.colors.onSecondaryContainer,
                                            ),
                                        )
                                    }

                                    Box(
                                        modifier = GlanceModifier.padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = valText,
                                            style = TextStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = cardColors.primary,
                                            ),
                                        )
                                    }

                                    // Plus Button
                                    Box(
                                        modifier = GlanceModifier
                                            .cornerRadius(6.dp)
                                            .background(GlanceTheme.colors.primary)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                            .clickable(actionSendBroadcast(incrementIntent)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "+",
                                            style = TextStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = GlanceTheme.colors.onPrimary,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        else -> {
            val previewText = when (note.type) {
                NoteType.MARKDOWN -> {
                    val parsed = getContentPreview(note.content)
                    parsed.ifBlank { "No content" }
                }
                NoteType.JOURNAL -> {
                    val entries = note.parseJournal().entries
                    if (entries.isEmpty()) "Journal entry"
                    else entries.lastOrNull()?.let { "${it.mood ?: ""} ${it.text}".trim() } ?: "Journal entry"
                }
                NoteType.VAULT -> "Vault Note (Protected)"
                else -> ""
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .cornerRadius(12.dp)
                    .background(cardColors.background)
                    .padding(10.dp)
                    .clickable(openNoteAction),
            ) {
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        text = titleText,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = cardColors.onSurface,
                        ),
                        maxLines = 1,
                        modifier = GlanceModifier.fillMaxWidth(),
                    )

                    Spacer(modifier = GlanceModifier.height(2.dp))

                    Text(
                        text = previewText,
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = cardColors.onSurfaceVariant,
                        ),
                        maxLines = 2,
                    )
                }
            }
        }
    }
}
