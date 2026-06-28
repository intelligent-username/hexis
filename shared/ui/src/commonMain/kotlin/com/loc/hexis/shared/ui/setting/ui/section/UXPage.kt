package com.loc.hexis.shared.ui.setting.ui.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.settings.Sections
import com.loc.hexis.shared.ui.components.ExpressiveSwitch
import com.loc.hexis.shared.ui.components.endItemShape
import com.loc.hexis.shared.ui.components.leadingItemShape
import com.loc.hexis.shared.ui.components.listItemColors
import com.loc.hexis.shared.ui.components.middleItemShape
import com.loc.hexis.shared.ui.setting.SettingsAction
import com.loc.hexis.shared.ui.setting.SettingsState
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import hexis.shared.ui.generated.resources.*
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun UXPage(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Column(
        modifier =
            Modifier.fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .background(MaterialTheme.colorScheme.background)
    ) {
        MediumFlexibleTopAppBar(
            scrollBehavior = scrollBehavior,
            title = {
                Text(
                    text = stringResource(Res.string.ux),
                    fontFamily = flexFontEmphasis(),
                )
            },
            navigationIcon = {
                FilledTonalIconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.nav_arrow_back),
                        contentDescription = "Navigate Back",
                    )
                }
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ListItem(
                        headlineContent = {
                            Text(text = stringResource(Res.string.pause_notifications))
                        },
                        supportingContent = {
                            Text(text = stringResource(Res.string.pause_notifications_desc))
                        },
                        trailingContent = {
                            ExpressiveSwitch(
                                checked = state.pauseNotifications,
                                onCheckedChange = {
                                    onAction(SettingsAction.ChangePauseNotifications(it))
                                },
                            )
                        },
                        colors = listItemColors(),
                        modifier = Modifier.clip(leadingItemShape()),
                    )

                    ListItem(
                        headlineContent = { Text(text = stringResource(Res.string.reorder_tasks)) },
                        supportingContent = {
                            Text(text = stringResource(Res.string.reorder_tasks_desc))
                        },
                        trailingContent = {
                            ExpressiveSwitch(
                                checked = state.reorderTasks,
                                onCheckedChange = {
                                    onAction(SettingsAction.ChangeReorderTasks(it))
                                },
                            )
                        },
                        colors = listItemColors(),
                        modifier = Modifier.clip(middleItemShape()),
                    )

                    ListItem(
                        headlineContent = { Text(text = stringResource(Res.string.show_habits)) },
                        supportingContent = {
                            Text(text = stringResource(Res.string.show_habits_desc))
                        },
                        trailingContent = {
                            ExpressiveSwitch(
                                checked = state.startingPage == Sections.Habits,
                                onCheckedChange = {
                                    onAction(
                                        SettingsAction.ChangeStartingPage(
                                            if (it) Sections.Habits else Sections.Tasks
                                        )
                                    )
                                },
                            )
                        },
                        colors = listItemColors(),
                        modifier = Modifier.clip(middleItemShape()),
                    )

                    ListItem(
                        headlineContent = { Text(text = stringResource(Res.string.staring_day)) },
                        trailingContent = {
                            ExpressiveSwitch(
                                checked = state.startOfTheWeek == DayOfWeek.SUNDAY,
                                onCheckedChange = {
                                    onAction(
                                        SettingsAction.ChangeStartOfTheWeek(
                                            if (it) DayOfWeek.SUNDAY else DayOfWeek.MONDAY
                                        )
                                    )
                                },
                            )
                        },
                        colors = listItemColors(),
                        modifier = Modifier.clip(middleItemShape()),
                    )

                    if (state.isBiometricLockAvailable) {
                        ListItem(
                            headlineContent = {
                                Text(text = stringResource(Res.string.biometric_lock))
                            },
                            supportingContent = {
                                Text(text = stringResource(Res.string.biometric_lock_desc))
                            },
                            trailingContent = {
                                ExpressiveSwitch(
                                    checked = state.isBiometricLockOn == true,
                                    onCheckedChange = {
                                        onAction(SettingsAction.ChangeBiometricLock(it))
                                    },
                                )
                            },
                            colors = listItemColors(),
                            modifier = Modifier.clip(middleItemShape()),
                        )
                    }

                    ListItem(
                        headlineContent = { Text(text = stringResource(Res.string.use_24Hr)) },
                        supportingContent = {
                            Text(text = stringResource(Res.string.use_24Hr_desc))
                        },
                        trailingContent = {
                            ExpressiveSwitch(
                                checked = state.is24Hr,
                                onCheckedChange = { onAction(SettingsAction.ChangeIs24Hr(it)) },
                            )
                        },
                        colors = listItemColors(),
                        modifier = Modifier.clip(endItemShape()),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    UXPage(
        state = SettingsState(),
        onAction = {},
        onNavigateBack = {},
    )
}
