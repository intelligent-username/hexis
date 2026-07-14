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

package com.loc.hexis.shared.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Compact
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.loc.hexis.shared.ui.LocalWindowSizeClass
import com.loc.hexis.shared.ui.app.AppSections.Companion.toIconRes
import com.loc.hexis.shared.ui.app.AppSections.Companion.toStringRes
import com.loc.hexis.shared.ui.habit.ui.HabitsGraph
import com.loc.hexis.shared.ui.setting.ui.SettingsGraph
import com.loc.hexis.shared.ui.note.ui.NotesPage
import com.loc.hexis.shared.ui.task.ui.TasksPage
import com.loc.hexis.shared.ui.task.ui.component.PomodoroPage
import com.loc.hexis.shared.ui.viewmodel.HabitViewModel
import com.loc.hexis.shared.ui.viewmodel.MainViewModel
import com.loc.hexis.shared.ui.viewmodel.SettingsViewModel
import com.loc.hexis.shared.ui.viewmodel.TasksViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainApp(state: MainAppState) {
    val windowSizeClass = LocalWindowSizeClass.current

    val pagerState =
        rememberPagerState(
            initialPage =
                when (state.startingSection) {
                    Tasks -> AppSections.mainRoutes.indexOf(AppSections.TaskPages).coerceAtLeast(0)
                    Habits ->
                        AppSections.mainRoutes.indexOf(AppSections.HabitPages).coerceAtLeast(0)
                },
            pageCount = { AppSections.mainRoutes.size },
        )
    val coroutineScope = rememberCoroutineScope()

    val mvm: MainViewModel = koinViewModel()
    val tvm: TasksViewModel = koinViewModel()
    val hvm: HabitViewModel = koinViewModel()
    val svm: SettingsViewModel = koinViewModel()

    var showPomodoro by remember { mutableStateOf(false) }
    var pomodoroLinkedHabitId by remember { mutableStateOf<Long?>(null) }
    var showNotes by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(state.shortcutAction) {
        state.shortcutAction?.let { action ->
            when (action) {
                "add_habit" -> {
                    pagerState.animateScrollToPage(
                        AppSections.mainRoutes.indexOf(AppSections.HabitPages).coerceAtLeast(0)
                    )
                    hvm.onAction(com.loc.hexis.shared.ui.habit.HabitsAction.OnAddHabitClicked)
                }
                "add_task" -> {
                    pagerState.animateScrollToPage(
                        AppSections.mainRoutes.indexOf(AppSections.TaskPages).coerceAtLeast(0)
                    )
                    tvm.onAction(com.loc.hexis.shared.ui.task.TaskAction.ToggleAddTaskSheet(true))
                }
                "overall_analytics" -> {
                    pagerState.animateScrollToPage(
                        AppSections.mainRoutes.indexOf(AppSections.HabitPages).coerceAtLeast(0)
                    )
                    hvm.onAction(com.loc.hexis.shared.ui.habit.HabitsAction.PrepareAnalytics(null))
                    hvm.onAction(
                        com.loc.hexis.shared.ui.habit.HabitsAction.ToggleOverallAnalytics(true)
                    )
                }
            }
            mvm.setShortcutAction(null)
        }
    }

    SystemBackHandler(enabled = true) {
        val current = pagerState.currentPage
        if (current > 0) {
            coroutineScope.launch { pagerState.animateScrollToPage(current - 1) }
        }
    }

    when (windowSizeClass.widthSizeClass) {
        Compact -> {
            Scaffold(
                bottomBar = {
                    AppNavBar(
                        currentRoute = AppSections.mainRoutes[pagerState.currentPage],
                        onNavigate = { route ->
                            val index = AppSections.mainRoutes.indexOf(route)
                            if (index != -1) {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            }
                        },
                    )
                }
            ) { padding ->
                HorizontalPager(
                    state = pagerState,
                    modifier =
                        Modifier.padding(
                                start = padding.calculateStartPadding(LocalLayoutDirection.current),
                                end = padding.calculateEndPadding(LocalLayoutDirection.current),
                                bottom = padding.calculateBottomPadding(),
                            )
                            .background(MaterialTheme.colorScheme.background),
                ) { page ->
                    when (val route = AppSections.mainRoutes[page]) {
                        is AppSections.TaskPages -> {
                            val taskPageState by tvm.state.collectAsStateWithLifecycle()

                            TasksPage(
                                state = taskPageState,
                                onAction = tvm::onAction,
                                onPomodoroClick = { showPomodoro = true },
                                onNotesClick = { showNotes = true },
                            )
                        }

                        is AppSections.SettingsPages -> {
                            val settingsState by svm.state.collectAsStateWithLifecycle()

                            SettingsGraph(state = settingsState, onAction = svm::onAction)
                        }

                        is AppSections.HabitPages -> {
                            val habitsPageState by hvm.state.collectAsStateWithLifecycle()

                            HabitsGraph(
                                state = habitsPageState,
                                onAction = hvm::onAction,
                                onPomodoroClick = { pomodoroLinkedHabitId = it },
                            )
                        }
                    }
                }
            }
        }

        else -> {
            Row(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                AppNavRail(
                    currentRoute = AppSections.mainRoutes[pagerState.currentPage],
                    onNavigate = { route ->
                        val index = AppSections.mainRoutes.indexOf(route)
                        if (index != -1) {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        }
                    },
                )

                HorizontalPager(
                    state = pagerState,
                    modifier =
                        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background),
                    verticalAlignment = Alignment.CenterVertically,
                ) { page ->
                    when (val route = AppSections.mainRoutes[page]) {
                        is AppSections.TaskPages -> {
                            val taskPageState by tvm.state.collectAsStateWithLifecycle()

                            TasksPage(
                                state = taskPageState,
                                onAction = tvm::onAction,
                                onPomodoroClick = { showPomodoro = true },
                                onNotesClick = { showNotes = true },
                            )
                        }

                        is AppSections.SettingsPages -> {
                            val settingsState by svm.state.collectAsStateWithLifecycle()

                            SettingsGraph(state = settingsState, onAction = svm::onAction)
                        }

                        is AppSections.HabitPages -> {
                            val habitsPageState by hvm.state.collectAsStateWithLifecycle()

                            HabitsGraph(
                                state = habitsPageState,
                                onAction = hvm::onAction,
                                onPomodoroClick = { pomodoroLinkedHabitId = it },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPomodoro || pomodoroLinkedHabitId != null) {
        PomodoroPage(
            linkedHabitId = if (showPomodoro) null else pomodoroLinkedHabitId,
            onDismiss = {
                showPomodoro = false
                pomodoroLinkedHabitId = null
            },
        )
    }

    if (showNotes) {
        NotesPage(onDismiss = { showNotes = false })
    }
}

@Composable
private fun AppNavRail(
    currentRoute: NavKey,
    onNavigate: (AppSections) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        AppSections.mainRoutes.forEach { route ->
            NavigationRailItem(
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        onNavigate(route)
                    }
                },
                icon = {
                    Icon(painter = painterResource(route.toIconRes()), contentDescription = null)
                },
                label = { Text(text = stringResource(route.toStringRes())) },
                alwaysShowLabel = false,
            )
        }
    }
}

@Composable
private fun AppNavBar(
    currentRoute: NavKey,
    onNavigate: (AppSections) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        AppSections.mainRoutes.forEach { route ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        onNavigate(route)
                    }
                },
                icon = {
                    Icon(painter = painterResource(route.toIconRes()), contentDescription = null)
                },
                label = { Text(text = stringResource(route.toStringRes())) },
                alwaysShowLabel = false,
            )
        }
    }
}
