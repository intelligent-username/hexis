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

package com.loc.hexis.shared.ui.setting.ui.section

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.hashPassword
import com.loc.hexis.core.settings.Sections
import com.loc.hexis.shared.ui.components.ExpressiveSwitch
import com.loc.hexis.shared.ui.components.HexisDialog
import com.loc.hexis.shared.ui.components.HexisTimePicker
import com.loc.hexis.shared.ui.components.detachedItemShape
import com.loc.hexis.shared.ui.components.endItemShape
import com.loc.hexis.shared.ui.components.leadingItemShape
import com.loc.hexis.shared.ui.components.listItemColors
import com.loc.hexis.shared.ui.components.middleItemShape
import com.loc.hexis.shared.ui.setting.SettingsAction
import com.loc.hexis.shared.ui.setting.SettingsState
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.*
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun UXPage(state: SettingsState, onAction: (SettingsAction) -> Unit, onNavigateBack: () -> Unit) {
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showCutoffTimePicker by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showVerifyDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Column(
        modifier =
            Modifier.fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .background(MaterialTheme.colorScheme.background)
    ) {
        MediumFlexibleTopAppBar(
            scrollBehavior = scrollBehavior,
            title = { Text(text = stringResource(Res.string.ux), fontFamily = flexFontEmphasis()) },
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
            // General Settings sub-section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "General",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                            modifier = Modifier.clip(leadingItemShape()),
                        )

                        ListItem(
                            headlineContent = { Text(text = stringResource(Res.string.starting_day)) },
                            supportingContent = {
                                Text(text = stringResource(Res.string.starting_day_desc))
                            },
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
                            modifier = Modifier.clip(middleItemShape()),
                        )

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
                            modifier =
                                Modifier.clip(
                                    if (state.isBiometricLockAvailable) middleItemShape()
                                    else endItemShape()
                                ),
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
                                modifier = Modifier.clip(endItemShape()),
                            )
                        }
                    }
                }
            }

            // Habit Settings sub-section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Habits",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        ListItem(
                            headlineContent = {
                                Text(text = stringResource(Res.string.reorder_habits))
                            },
                            supportingContent = {
                                Text(text = stringResource(Res.string.reorder_habits_desc))
                            },
                            trailingContent = {
                                ExpressiveSwitch(
                                    checked = state.reorderHabits,
                                    onCheckedChange = {
                                        onAction(SettingsAction.ChangeReorderHabits(it))
                                    },
                                )
                            },
                            colors = listItemColors(),
                            modifier = Modifier.clip(leadingItemShape()),
                        )

                        val formattedCutoffHour =
                            if (state.is24Hr)
                                "${state.dayCutoffHour.toString().padStart(2, '0')}:00"
                            else
                                "${if (state.dayCutoffHour == 0) 12 else if (state.dayCutoffHour > 12) state.dayCutoffHour - 12 else state.dayCutoffHour}:00 ${if (state.dayCutoffHour >= 12) "PM" else "AM"}"

                        ListItem(
                            headlineContent = { Text(text = "Day Cutoff Hour") },
                            supportingContent = {
                                Text(
                                    text =
                                        if (state.isDayCutoffEnabled)
                                            "Day resets at $formattedCutoffHour. Tap to change time."
                                        else "Day resets at midnight (00:00)"
                                )
                            },
                            trailingContent = {
                                ExpressiveSwitch(
                                    checked = state.isDayCutoffEnabled,
                                    onCheckedChange = { enabled ->
                                        onAction(SettingsAction.ChangeDayCutoffEnabled(enabled))
                                        if (enabled) {
                                            showCutoffTimePicker = true
                                        }
                                    },
                                )
                            },
                            colors = listItemColors(),
                            modifier =
                                Modifier.clip(endItemShape()).clickable {
                                    if (state.isDayCutoffEnabled) {
                                        showCutoffTimePicker = true
                                    }
                                },
                        )
                    }
                }
            }

            // Tasks & Pomodoro Settings sub-section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Tasks",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                            modifier = Modifier.clip(leadingItemShape()),
                        )

                        ListItem(
                            headlineContent = { Text(text = "Show pie chart in Pomodoro History tab") },
                            supportingContent = {
                                Text(text = "Display category breakdown pie chart in Pomodoro history")
                            },
                            trailingContent = {
                                ExpressiveSwitch(
                                    checked = state.showPomodoroPieChart,
                                    onCheckedChange = {
                                        onAction(SettingsAction.ChangeShowPomodoroPieChart(it))
                                    },
                                )
                            },
                            colors = listItemColors(),
                            modifier = Modifier.clip(middleItemShape()),
                        )

                        val hasVaultPasswordChange =
                            state.vaultPasswordHash != null && state.isLockVaultNotesOn

                        ListItem(
                            headlineContent = { Text(text = "Lock Vault Notes") },
                            supportingContent = {
                                Text(text = "Require password to view and edit encrypted vault notes")
                            },
                            trailingContent = {
                                ExpressiveSwitch(
                                    checked = state.isLockVaultNotesOn,
                                    onCheckedChange = { enable ->
                                        if (enable) {
                                            if (state.vaultPasswordHash == null) {
                                                newPassword = ""
                                                passwordError = null
                                                showPasswordDialog = true
                                            } else {
                                                onAction(SettingsAction.ChangeLockVaultNotes(true))
                                            }
                                        } else {
                                            onAction(SettingsAction.ChangeLockVaultNotes(false))
                                        }
                                    },
                                )
                            },
                            colors = listItemColors(),
                            modifier =
                                Modifier.clip(
                                    if (hasVaultPasswordChange) middleItemShape()
                                    else endItemShape()
                                ),
                        )

                        if (hasVaultPasswordChange) {
                            ListItem(
                                headlineContent = { Text(text = "Change Vault Password") },
                                supportingContent = {
                                    Text(
                                        text =
                                            "Authenticate with biometrics or device PIN to set a new password"
                                    )
                                },
                                trailingContent = {
                                    TextButton(onClick = { showVerifyDialog = true }) { Text("Change") }
                                },
                                colors = listItemColors(),
                                modifier = Modifier.clip(endItemShape()),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPasswordDialog) {
        HexisDialog(onDismissRequest = { showPasswordDialog = false }) {
            Text(
                text = "Set Vault Password",
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
            )
            Text(
                text =
                    "Enter a password to lock your vault notes. Password is saved locally as an encrypted hash.",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = flexFontRounded()),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    passwordError = null
                },
                placeholder = { Text("Vault Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null,
            )
            if (passwordError != null) {
                Text(
                    text = passwordError!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel") }
                Button(
                    onClick = {
                        if (newPassword.trim().isEmpty()) {
                            passwordError = "Password cannot be empty"
                        } else {
                            val hash = hashPassword(newPassword.trim())
                            onAction(SettingsAction.SetVaultPasswordHash(hash))
                            onAction(SettingsAction.ChangeLockVaultNotes(true))
                            showPasswordDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }

    if (showVerifyDialog) {
        HexisDialog(onDismissRequest = { showVerifyDialog = false }) {
            Text(
                text = "Biometric / Device Lock Verification",
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
            )
            Text(
                text = "Authorize password change using your device biometric or PIN lock.",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = flexFontRounded()),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextButton(onClick = { showVerifyDialog = false }) { Text("Cancel") }
                Button(
                    onClick = {
                        showVerifyDialog = false
                        newPassword = ""
                        passwordError = null
                        showPasswordDialog = true
                    }
                ) {
                    Text("Authenticate & Set Password")
                }
            }
        }
    }

    if (showCutoffTimePicker) {
        val cutoffTimePickerState =
            rememberTimePickerState(
                initialHour = state.dayCutoffHour,
                initialMinute = 0,
                is24Hour = state.is24Hr,
            )
        HexisTimePicker(
            onDismissRequest = { showCutoffTimePicker = false },
            state = cutoffTimePickerState,
            onConfirm = {
                onAction(SettingsAction.ChangeDayCutoffHour(cutoffTimePickerState.hour))
                onAction(SettingsAction.ChangeDayCutoffEnabled(true))
                showCutoffTimePicker = false
            },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    UXPage(state = SettingsState(), onAction = {}, onNavigateBack = {})
}
