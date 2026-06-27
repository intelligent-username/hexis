package com.shub39.grit.app

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.shub39.grit.di.HexisModules
import com.shub39.grit.widgets.all_tasks_widget.AllTasksWidgetReceiver
import com.shub39.grit.widgets.habit_overview_widget.HabitOverviewWidgetReceiver
import com.shub39.grit.widgets.habit_streak_widget.HabitStreakWidgetReceiver
import com.shub39.grit.widgets.habit_week_chart_widget.HabitWeekChartWidgetReceiver
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