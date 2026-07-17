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

package com.loc.hexis.shared.ui.setting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.loc.hexis.shared.ui.HexisPreviewWrapper
import com.loc.hexis.shared.ui.components.PageFill
import com.loc.hexis.shared.ui.navigation.horizontalTransitionMetadata
import com.loc.hexis.shared.ui.setting.SettingsAction
import com.loc.hexis.shared.ui.setting.SettingsState
import com.loc.hexis.shared.ui.setting.ui.section.About
import com.loc.hexis.shared.ui.setting.ui.section.BackupPage
import com.loc.hexis.shared.ui.setting.ui.section.Changelog
import com.loc.hexis.shared.ui.setting.ui.section.LookAndFeelPage
import com.loc.hexis.shared.ui.setting.ui.section.RootPage
import com.loc.hexis.shared.ui.setting.ui.section.UXPage
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
private sealed interface SettingsRoutes : NavKey {
    @Serializable data object Root : SettingsRoutes

    @Serializable data object UX : SettingsRoutes

    @Serializable data object LookAndFeel : SettingsRoutes

    @Serializable data object Backup : SettingsRoutes

    @Serializable data object Changelog : SettingsRoutes

    @Serializable data object About : SettingsRoutes
}

private val configuration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(SettingsRoutes.Root::class, SettingsRoutes.Root.serializer())
            subclass(SettingsRoutes.UX::class, SettingsRoutes.UX.serializer())
            subclass(SettingsRoutes.LookAndFeel::class, SettingsRoutes.LookAndFeel.serializer())
            subclass(SettingsRoutes.Backup::class, SettingsRoutes.Backup.serializer())
            subclass(SettingsRoutes.Changelog::class, SettingsRoutes.Changelog.serializer())
            subclass(SettingsRoutes.About::class, SettingsRoutes.About.serializer())
        }
    }
}

@Composable
fun SettingsGraph(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) =
    PageFill(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        val backStack = rememberNavBackStack(configuration, SettingsRoutes.Root)

        NavDisplay(
            modifier = Modifier.widthIn(max = 600.dp).fillMaxSize(),
            backStack = backStack,
            entryProvider =
                entryProvider {
                    entry<SettingsRoutes.Root> {
                        RootPage(
                            state = state,
                            onAction = onAction,
                            onNavigateToUX = { backStack.add(SettingsRoutes.UX) },
                            onNavigateToLookAndFeel = { backStack.add(SettingsRoutes.LookAndFeel) },
                            onNavigateToBackup = { backStack.add(SettingsRoutes.Backup) },
                            onNavigateToChangelog = { backStack.add(SettingsRoutes.Changelog) },
                            onNavigateToAppInfo = { backStack.add(SettingsRoutes.About) },
                        )
                    }

                    entry<SettingsRoutes.UX>(metadata = horizontalTransitionMetadata()) {
                        UXPage(
                            state = state,
                            onAction = onAction,
                            onNavigateBack = {
                                if (backStack.size != 1) backStack.removeLastOrNull()
                            },
                        )
                    }

                    entry<SettingsRoutes.LookAndFeel>(metadata = horizontalTransitionMetadata()) {
                        LookAndFeelPage(
                            state = state,
                            onAction = onAction,
                            onNavigateBack = {
                                if (backStack.size != 1) backStack.removeLastOrNull()
                            },
                        )
                    }

                    entry<SettingsRoutes.Backup>(metadata = horizontalTransitionMetadata()) {
                        BackupPage(
                            state = state,
                            onAction = onAction,
                            onNavigateBack = {
                                if (backStack.size != 1) backStack.removeLastOrNull()
                            },
                        )
                    }

                    entry<SettingsRoutes.Changelog>(metadata = horizontalTransitionMetadata()) {
                        Changelog(
                            changelog = state.changelog,
                            onNavigateBack = {
                                if (backStack.size != 1) backStack.removeLastOrNull()
                            },
                        )
                    }

                    entry<SettingsRoutes.About>(metadata = horizontalTransitionMetadata()) {
                        About(
                            versionName = state.currentVersion ?: "1.0.00-Demo",
                            onNavigateBack = {
                                if (backStack.size != 1) backStack.removeLastOrNull()
                            },
                        )
                    }
                },
        )
    }

@PreviewWrapper(HexisPreviewWrapper::class)
@Preview
@Composable
private fun Preview() {
    SettingsGraph(state = SettingsState(), onAction = {})
}
