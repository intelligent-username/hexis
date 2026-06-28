package com.loc.hexis.shared.ui.setting.ui.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.loc.hexis.shared.ui.HexisPreviewWrapper
import com.loc.hexis.shared.ui.components.detachedItemShape
import com.loc.hexis.shared.ui.components.endItemShape
import com.loc.hexis.shared.ui.components.leadingItemShape
import com.loc.hexis.shared.ui.components.listItemColors
import com.loc.hexis.shared.ui.setting.SettingsAction
import com.loc.hexis.shared.ui.setting.SettingsState
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import hexis.shared.ui.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

/** Root settings page all roads start from here */
@Composable
fun RootPage(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    onNavigateToUX: () -> Unit,
    onNavigateToLookAndFeel: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToChangelog: () -> Unit,
    onNavigateToAppInfo: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Column(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).fillMaxSize()) {
        LargeFlexibleTopAppBar(
            scrollBehavior = scrollBehavior,
            title = {
                Text(text = stringResource(Res.string.settings), fontFamily = flexFontEmphasis())
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // UX settings
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ListItem(
                        modifier =
                            Modifier.clip(detachedItemShape()).clickable { onNavigateToUX() },
                        headlineContent = { Text(text = stringResource(Res.string.ux)) },
                        supportingContent = {
                            Text(text = stringResource(Res.string.ux_desc))
                        },
                        trailingContent = {
                            Icon(
                                imageVector = vectorResource(Res.drawable.arrow_forward),
                                contentDescription = "Navigate",
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = vectorResource(Res.drawable.settings),
                                contentDescription = "UX",
                            )
                        },
                        colors = listItemColors(),
                    )
                }
            }

            // look and feel customizations
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ListItem(
                        modifier =
                            Modifier.clip(leadingItemShape()).clickable {
                                onNavigateToLookAndFeel()
                            },
                        headlineContent = { Text(text = stringResource(Res.string.look_and_feel)) },
                        supportingContent = {
                            Text(text = stringResource(Res.string.look_and_feel_desc))
                        },
                        trailingContent = {
                            Icon(
                                imageVector = vectorResource(Res.drawable.arrow_forward),
                                contentDescription = "Navigate",
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = vectorResource(Res.drawable.palette),
                                contentDescription = "Navigate",
                            )
                        },
                        colors = listItemColors(),
                    )

                    ListItem(
                        modifier = Modifier.clip(endItemShape()).clickable { onNavigateToBackup() },
                        colors = listItemColors(),
                        headlineContent = { Text(text = stringResource(Res.string.backup)) },
                        supportingContent = { Text(text = stringResource(Res.string.backup_desc)) },
                        trailingContent = {
                            Icon(
                                imageVector = vectorResource(Res.drawable.arrow_forward),
                                contentDescription = "Navigate",
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = vectorResource(Res.drawable.download),
                                contentDescription = "Backup",
                            )
                        },
                    )
                }
            }

            // Changelogs
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ListItem(
                        colors = listItemColors(),
                        leadingContent = {
                            Icon(
                                imageVector = vectorResource(Res.drawable.info),
                                contentDescription = null,
                            )
                        },
                        supportingContent = {
                            Text(text = "Hexis ${state.currentVersion ?: "x.x.x"}")
                        },
                        trailingContent = {
                            Icon(
                                imageVector = vectorResource(Res.drawable.arrow_forward),
                                contentDescription = "Navigate",
                            )
                        },
                        headlineContent = { Text(text = stringResource(Res.string.about)) },
                        modifier =
                            Modifier.clip(leadingItemShape()).clickable { onNavigateToAppInfo() },
                    )

                    ListItem(
                        colors = listItemColors(),
                        leadingContent = {
                            Icon(
                                imageVector = vectorResource(Res.drawable.check_list),
                                contentDescription = null,
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = vectorResource(Res.drawable.arrow_forward),
                                contentDescription = "Navigate",
                            )
                        },
                        headlineContent = { Text(text = stringResource(Res.string.changelog)) },
                        modifier =
                            Modifier.clip(endItemShape()).clickable { onNavigateToChangelog() },
                    )
                }
            }

        }
    }
}

@PreviewWrapper(HexisPreviewWrapper::class)
@PreviewLightDark
@Composable
private fun Preview() {
    RootPage(
        state = SettingsState(),
        onAction = {},
        onNavigateToUX = {},
        onNavigateToLookAndFeel = {},
        onNavigateToBackup = {},
        onNavigateToChangelog = {},
        onNavigateToAppInfo = {},
    )
}