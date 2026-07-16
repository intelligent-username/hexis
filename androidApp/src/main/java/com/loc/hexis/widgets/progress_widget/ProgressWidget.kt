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

package com.loc.hexis.widgets.progress_widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.loc.hexis.app.MainActivity
import com.loc.hexis.core.habits.HabitRepo
import com.loc.hexis.core.habits.OverallAnalytics
import com.loc.hexis.core.habits.PointsTrend
import com.loc.hexis.core.interfaces.ThemeDatastore
import com.loc.hexis.core.interfaces.WidgetActions
import com.loc.hexis.widgets.rememberWidgetColorProviders
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class ProgressWidget : GlanceAppWidget(), KoinComponent {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = get<HabitRepo>()
        val themeDatastore = get<ThemeDatastore>()

        provideContent {
            val appTheme by
                themeDatastore
                    .getAppThemeFlow()
                    .collectAsState(com.loc.hexis.core.theme.AppTheme.SYSTEM)
            val seedColor by themeDatastore.getSeedColorFlow().collectAsState(0xFFFFFF)
            val isAmoled by themeDatastore.getAmoledPref().collectAsState(false)
            val paletteStyle by
                themeDatastore
                    .getPaletteStyle()
                    .collectAsState(com.loc.hexis.core.theme.PaletteStyle.TONALSPOT)
            val isMaterialYou by themeDatastore.getMaterialYouFlow().collectAsState(false)

            val colors =
                rememberWidgetColorProviders(
                    appTheme = appTheme,
                    seedColor = seedColor,
                    isAmoled = isAmoled,
                    paletteStyle = paletteStyle,
                    isMaterialYou = isMaterialYou,
                )

            val analytics by repo.getOverallAnalytics().collectAsState(OverallAnalytics())
            val trend by repo.getPointsTrend().collectAsState(PointsTrend.empty)

            val openAction =
                androidx.glance.appwidget.action.actionStartActivity(
                    Intent(context, MainActivity::class.java).apply {
                        putExtra("shortcut_action", WidgetActions.OPEN_PROGRESS)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                )

            GlanceTheme(colors = colors) {
                ProgressWidgetContent(
                    analytics = analytics,
                    trend = trend,
                    onRefresh = {},
                    onOpenApp = openAction,
                )
            }
        }
    }
}
