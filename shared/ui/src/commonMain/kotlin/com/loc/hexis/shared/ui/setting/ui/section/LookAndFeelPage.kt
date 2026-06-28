package com.loc.hexis.shared.ui.setting.ui.section

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.theme.AppTheme
import com.loc.hexis.core.theme.Fonts
import com.loc.hexis.core.theme.PaletteStyle
import com.loc.hexis.core.theme.Theme
import com.loc.hexis.shared.ui.components.ColorPickerDialog
import com.loc.hexis.shared.ui.components.ExpressiveSwitch
import com.loc.hexis.shared.ui.components.endItemShape
import com.loc.hexis.shared.ui.components.leadingItemShape
import com.loc.hexis.shared.ui.components.listItemColors
import com.loc.hexis.shared.ui.components.middleItemShape
import com.loc.hexis.shared.ui.setting.SettingsAction
import com.loc.hexis.shared.ui.setting.SettingsState
import com.loc.hexis.shared.ui.theme.HexisTheme
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.toDisplayString
import com.loc.hexis.shared.ui.toFontRes
import hexis.shared.ui.generated.resources.*
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun LookAndFeelPage(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    var colorPickerDialog by remember { mutableStateOf(false) }

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
                    text = stringResource(Res.string.look_and_feel),
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
        ) {
            item {
                // appTheme picker
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Column(modifier = Modifier.clip(leadingItemShape())) {
                        ListItem(
                            leadingContent = {
                                Icon(
                                    imageVector =
                                        vectorResource(
                                            when (state.theme.appTheme) {
                                                SYSTEM -> {
                                                    if (isSystemInDarkTheme())
                                                        Res.drawable.dark_mode
                                                    else Res.drawable.light_mode
                                                }

                                                DARK -> Res.drawable.dark_mode
                                                LIGHT -> Res.drawable.light_mode
                                            }
                                        ),
                                    contentDescription = null,
                                )
                            },
                            headlineContent = { Text(text = stringResource(Res.string.app_theme)) },
                            colors = listItemColors(),
                        )

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                            modifier =
                                Modifier.fillParentMaxWidth()
                                    .background(listItemColors().containerColor)
                                    .padding(start = 52.dp, end = 16.dp, bottom = 8.dp),
                        ) {
                            AppTheme.entries.forEach { appTheme ->
                                ToggleButton(
                                    checked = appTheme == state.theme.appTheme,
                                    onCheckedChange = {
                                        onAction(SettingsAction.ChangeAppTheme(appTheme))
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors =
                                        ToggleButtonDefaults.toggleButtonColors(
                                            containerColor =
                                                MaterialTheme.colorScheme.surfaceContainerLow
                                        ),
                                ) {
                                    Text(text = stringResource(appTheme.toDisplayString()))
                                }
                            }
                        }
                    }

                    MaterialYouToggle(
                        isMaterialYou = state.theme.isMaterialYou,
                        onClick = { onAction(SettingsAction.ChangeMaterialYou(it)) },
                    )

                    // font picker
                    Column(
                        modifier =
                            Modifier.clip(
                                if (state.theme.isMaterialYou) endItemShape()
                                else middleItemShape()
                            )
                    ) {
                        ListItem(
                            headlineContent = { Text(text = stringResource(Res.string.font)) },
                            leadingContent = {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.font),
                                    contentDescription = null,
                                )
                            },
                            colors = listItemColors(),
                        )

                        FlowRow(
                            modifier =
                                Modifier.fillParentMaxWidth()
                                    .background(listItemColors().containerColor)
                                    .padding(start = 52.dp, end = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Fonts.entries.forEach { font ->
                                ToggleButton(
                                    checked = state.theme.font == font,
                                    onCheckedChange = {
                                        onAction(SettingsAction.ChangeFontPref(font))
                                    },
                                    colors =
                                        ToggleButtonDefaults.toggleButtonColors(
                                            containerColor =
                                                MaterialTheme.colorScheme.surfaceContainerLow
                                        ),
                                ) {
                                    Text(
                                        text = font.toDisplayString(),
                                        fontFamily =
                                            font.toFontRes()?.let { FontFamily(Font(it)) }
                                                ?: FontFamily.Default,
                                    )
                                }
                            }
                        }
                    }

                    if (!state.theme.isMaterialYou) {
                        // amoled toggle
                        ListItem(
                            headlineContent = {
                                Text(text = stringResource(Res.string.use_amoled))
                            },
                            supportingContent = {
                                Text(text = stringResource(Res.string.use_amoled_desc))
                            },
                            trailingContent = {
                                ExpressiveSwitch(
                                    checked = state.theme.isAmoled,
                                    onCheckedChange = { onAction(SettingsAction.ChangeAmoled(it)) },
                                )
                            },
                            colors = listItemColors(),
                            modifier = Modifier.clip(middleItemShape()),
                        )

                        // seed color picker
                        ListItem(
                            headlineContent = {
                                Text(text = stringResource(Res.string.select_seed))
                            },
                            supportingContent = {
                                Text(text = stringResource(Res.string.select_seed_desc))
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = { colorPickerDialog = true },
                                    colors =
                                        IconButtonDefaults.iconButtonColors(
                                            containerColor = Color(state.theme.seedColor),
                                            contentColor =
                                                contentColorFor(Color(state.theme.seedColor)),
                                        ),
                                ) {
                                    Icon(
                                        imageVector = vectorResource(Res.drawable.edit),
                                        contentDescription = "Select Color",
                                    )
                                }
                            },
                            colors = listItemColors(),
                            modifier = Modifier.clip(middleItemShape()),
                        )

                        // palette style picker
                        PaletteStylePicker(
                            paletteStyle = state.theme.paletteStyle,
                            isMaterialYou = state.theme.isMaterialYou,
                            seedColor = Color(state.theme.seedColor),
                            appTheme = state.theme.appTheme,
                            isAmoled = state.theme.isAmoled,
                            onClick = { onAction(SettingsAction.ChangePaletteStyle(it)) },
                        )
                    }
                }
            }
        }
    }

    if (colorPickerDialog) {
        ColorPickerDialog(
            initialColor = Color(state.theme.seedColor),
            onSelect = { onAction(SettingsAction.ChangeSeedColor(it)) },
            onDismiss = { colorPickerDialog = false },
        )
    }
}

@Composable
expect fun MaterialYouToggle(
    isMaterialYou: Boolean,
    onClick: (Boolean) -> Unit,
)

@Composable
expect fun PaletteStylePicker(
    paletteStyle: PaletteStyle,
    isMaterialYou: Boolean,
    seedColor: Color,
    appTheme: AppTheme,
    isAmoled: Boolean,
    onClick: (PaletteStyle) -> Unit,
)

@Preview
@Composable
private fun Preview() {
    HexisTheme(theme = Theme(appTheme = AppTheme.DARK)) {
        Surface {
            LookAndFeelPage(
                state = SettingsState(),
                onAction = {},
                onNavigateBack = {},
            )
        }
    }
}