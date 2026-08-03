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

package com.loc.hexis.app

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.loc.hexis.di.HexisModules
import com.loc.hexis.widgets.all_tasks_widget.AllTasksWidgetReceiver
import com.loc.hexis.widgets.habit_overview_widget.HabitOverviewWidgetReceiver
import com.loc.hexis.widgets.habit_streak_widget.HabitStreakWidgetReceiver
import com.loc.hexis.widgets.habit_week_chart_widget.HabitWeekChartWidgetReceiver
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.plugin.module.dsl.startKoin

class HexisApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin<HexisModules> {
            androidLogger()
            androidContext(this@HexisApplication)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val manager = GlanceAppWidgetManager(applicationContext)

            @SuppressLint("CheckResult")
            MainScope().launch {
                manager.setWidgetPreviews(HabitOverviewWidgetReceiver::class)
                manager.setWidgetPreviews(HabitStreakWidgetReceiver::class)
                manager.setWidgetPreviews(AllTasksWidgetReceiver::class)
                manager.setWidgetPreviews(HabitWeekChartWidgetReceiver::class)
            }
        }
    }
}
