package com.loc.hexis.widgets.notes_shortcut_widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.loc.hexis.R
import com.loc.hexis.app.MainActivity
import com.loc.hexis.core.interfaces.WidgetActions
import com.loc.hexis.widgets.rememberWidgetColorProviders
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class NotesShortcutWidget : GlanceAppWidget(), KoinComponent {

    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val themeDatastore = get<com.loc.hexis.core.interfaces.ThemeDatastore>()

        provideContent {
            val appTheme by themeDatastore.getAppThemeFlow().collectAsState(
                com.loc.hexis.core.theme.AppTheme.SYSTEM
            )
            val seedColor by themeDatastore.getSeedColorFlow().collectAsState(0xFFFFFF)
            val isAmoled by themeDatastore.getAmoledPref().collectAsState(false)
            val paletteStyle by themeDatastore.getPaletteStyle().collectAsState(
                com.loc.hexis.core.theme.PaletteStyle.TONALSPOT
            )
            val isMaterialYou by themeDatastore.getMaterialYouFlow().collectAsState(false)

            val colors = rememberWidgetColorProviders(
                appTheme = appTheme,
                seedColor = seedColor,
                isAmoled = isAmoled,
                paletteStyle = paletteStyle,
                isMaterialYou = isMaterialYou,
            )

            GlanceTheme(colors = colors) {
                NotesContent()
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            val colors = rememberWidgetColorProviders(
                appTheme = com.loc.hexis.core.theme.AppTheme.LIGHT,
                seedColor = 0xFFFFFF,
                isAmoled = false,
                paletteStyle = com.loc.hexis.core.theme.PaletteStyle.TONALSPOT,
                isMaterialYou = false,
            )
            GlanceTheme(colors = colors) {
                NotesContent()
            }
        }
    }
}

@GlanceComposable
@Composable
private fun NotesContent() {
    val context = LocalContext.current

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(
                actionStartActivity(
                    Intent(context, MainActivity::class.java).apply {
                        putExtra("shortcut_action", WidgetActions.OPEN_NOTES)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = GlanceModifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_shortcut_notes),
                contentDescription = "Notes",
                modifier = GlanceModifier.padding(bottom = 4.dp),
            )
            Text(
                text = "Notes",
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurface,
                ),
                maxLines = 1,
            )
        }
    }
}
