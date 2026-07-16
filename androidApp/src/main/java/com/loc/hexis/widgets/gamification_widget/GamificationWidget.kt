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

package com.loc.hexis.widgets.gamification_widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceAppWidget
import androidx.glance.GlanceId
import androidx.glance.appwidget.provideContent
import com.loc.hexis.app.MainActivity
import com.loc.hexis.core.habits.HabitRepo
import com.loc.hexis.core.interfaces.WidgetActions
import com.loc.hexis.core.theme.AppTheme
import com.loc.hexis.core.theme.PaletteStyle
import com.loc.hexis.core.interfaces.SettingsDatastore
import com.loc.hexis.core.interfaces.ThemeDatastore
import com.loc.hexis.widgets.rememberWidgetColorProviders
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class GamificationWidget : GlanceAppWidget(), KoinComponent {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = get<HabitRepo>()
        val themeDatastore = get<ThemeDatastore>()
        val settingsDatastore = get<SettingsDatastore>()
        val firstDayOfWeek = settingsDatastore.getStartOfTheWeekPref().first()

        provideContent {
            val appTheme by themeDatastore.getAppThemeFlow().collectAsState(AppTheme.SYSTEM)
            val seedColor by themeDatastore.getSeedColorFlow().collectAsState(0xFFFFFF)
            val isAmoled by themeDatastore.getAmoledPref().collectAsState(false)
            val paletteStyle by themeDatastore.getPaletteStyle().collectAsState(PaletteStyle.TONALSPOT)
            val isMaterialYou by themeDatastore.getMaterialYouFlow().collectAsState(false)

            val colors = rememberWidgetColorProviders(
                appTheme = appTheme,
                seedColor = seedColor,
                isAmoled = isAmoled,
                paletteStyle = paletteStyle,
                isMaterialYou = isMaterialYou,
            )

            val analytics by repo.getOverallAnalytics().collectAsState(com.loc.hexis.core.habits.OverallAnalytics())
            val trend by repo.getPointsTrend().collectAsState(com.loc.hexis.core.habits.PointsTrend.empty)

            val openAction = androidx.glance.action.actionStartActivity<MainActivity>(
                Intent(context, MainActivity::class.java).apply {
                    putExtra("shortcut_action", WidgetActions.OPEN_GAMIFICATION)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            )

            com.loc.hexis.shared.ui.theme.GlanceTheme(colors = colors) {
                GamificationWidgetContent(
                    analytics = analytics,
                    trend = trend,
                    onRefresh = { update(context, id) },
                    onOpenApp = openAction,
                )
            }
        }
    }
}
