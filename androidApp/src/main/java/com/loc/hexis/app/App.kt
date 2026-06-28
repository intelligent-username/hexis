package com.loc.hexis.app

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.loc.hexis.shared.ui.app.MainApp
import com.loc.hexis.shared.ui.app.MainAppState
import com.loc.hexis.shared.ui.components.ChangelogSheet
import kotlinx.serialization.Serializable

private sealed interface GlobalRoutes : NavKey {
    @Serializable data object App : GlobalRoutes
}

@Composable
fun App(state: MainAppState, onDismissChangelog: () -> Unit) {
    val mainBackStack = rememberNavBackStack(GlobalRoutes.App)

    if (state.currentChangelog != null) {
        ChangelogSheet(currentLog = state.currentChangelog!!, onDismissRequest = onDismissChangelog)
    }

    NavDisplay(
        backStack = mainBackStack,
        entryProvider =
            entryProvider {
                entry<GlobalRoutes.App> { MainApp(state = state) }
            },
    )
}
