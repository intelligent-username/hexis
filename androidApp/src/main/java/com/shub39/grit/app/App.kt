package com.shub39.grit.app

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.shub39.grit.shared.ui.app.MainApp
import com.shub39.grit.shared.ui.app.MainAppState
import com.shub39.grit.shared.ui.components.ChangelogSheet
import com.shub39.grit.shared.ui.navigation.verticalTransitionMetadata
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
