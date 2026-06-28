package com.loc.hexis.shared.ui.setting.ui.section

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import com.loc.hexis.shared.ui.components.detachedItemShape
import com.loc.hexis.shared.ui.components.endItemShape
import com.loc.hexis.shared.ui.components.leadingItemShape
import com.loc.hexis.shared.ui.components.middleItemShape
import com.loc.hexis.shared.ui.components.listItemColors
import com.loc.hexis.shared.ui.setting.ui.component.LicenseBottomSheet
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun About(versionName: String, onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val uriHandler = LocalUriHandler.current

    var showLicenseBottomSheet by remember { mutableStateOf(false) }

    if (showLicenseBottomSheet) {
        LicenseBottomSheet(onDismissRequest = { showLicenseBottomSheet = false })
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(text = stringResource(Res.string.about), fontFamily = flexFontEmphasis())
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
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding =
                PaddingValues(
                    top = padding.calculateTopPadding() + 16.dp,
                    bottom = padding.calculateBottomPadding() + 60.dp,
                    start = padding.calculateLeftPadding(LocalLayoutDirection.current) + 16.dp,
                    end = padding.calculateRightPadding(LocalLayoutDirection.current) + 16.dp,
                ),
        ) {
            aboutApp(versionName = versionName, uriHandler = uriHandler)

            item {
                ListItem(
                    colors = listItemColors(),
                    leadingContent = {
                        Icon(
                            imageVector = vectorResource(Res.drawable.license),
                            contentDescription = null,
                        )
                    },
                    headlineContent = { Text(text = "License") },
                    supportingContent = { Text(text = "GPL-3.0 License") },
                    modifier =
                        Modifier.clip(detachedItemShape()).clickable {
                            showLicenseBottomSheet = true
                        },
                )
            }
        }
    }
}

private fun LazyListScope.aboutApp(versionName: String, uriHandler: UriHandler) {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // App card
            Card(shape = leadingItemShape()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier.size(64.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialShapes.Cookie12Sided.toShape(),
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.hexis_logo),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hexis",
                            style =
                                MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = flexFontRounded()
                                ),
                        )
                        Text(
                            text = versionName,
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.primary
                                ),
                        )
                    }

                    Row {

                        FilledTonalIconButton(
                            onClick = { uriHandler.openUri("https://github.com/intelligent-username/hexis") }
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.github),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }

            // Abstract card
            Card(shape = middleItemShape()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "The name Hexis is inspired by Aristotle's quote:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "\"We are what we repeatedly do. Excellence, then, is not an act, but a habit.\"",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "The word for excellence is Ἀρετή (arete), and the word for habitual practice of excellence is Ἕξις (Hexis). The intention of this app is to cultivate excellence in the user.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Open source card
            Card(shape = middleItemShape()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    val annotatedString = buildAnnotatedString {
                        append("This app is ")
                        
                        pushStringAnnotation(tag = "open_source", annotation = "https://github.com/intelligent-username/Hexis")
                        withStyle(style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )) {
                            append("open source")
                        }
                        pop()
                        
                        append(". You can view the source code, ")
                        
                        pushStringAnnotation(tag = "issues", annotation = "https://github.com/intelligent-username/Hexis/issues")
                        withStyle(style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )) {
                            append("report bugs or request features")
                        }
                        pop()
                        
                        append(", and contribute by modifying the app and creating pull requests at our GitHub repository.")
                    }
                    
                    ClickableText(
                        text = annotatedString,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        onClick = { offset ->
                            annotatedString.getStringAnnotations(tag = "open_source", start = offset, end = offset)
                                .firstOrNull()?.let { uriHandler.openUri(it.item) }
                            annotatedString.getStringAnnotations(tag = "issues", start = offset, end = offset)
                                .firstOrNull()?.let { uriHandler.openUri(it.item) }
                        }
                    )
                }
            }

            // dev card
            Card(shape = endItemShape()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier =
                                Modifier.size(64.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = MaterialShapes.Square.toShape(),
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.dev_icon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(48.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "intelligent-username",
                                style =
                                    MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = flexFontRounded()
                                    ),
                            )
                            Text(
                                text = "Developer",
                                style =
                                    MaterialTheme.typography.titleSmall.copy(
                                        color = MaterialTheme.colorScheme.tertiary
                                    ),
                            )
                        }
                    }

                    FlowRow(
                        modifier = Modifier.padding(start = 80.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(
                                "https://github.com/intelligent-username" to Res.drawable.github,
                                "https://varak.dev/" to Res.drawable.language,

                            )
                            .forEach { pair ->
                                FilledTonalIconButton(
                                    onClick = { uriHandler.openUri(pair.first) },
                                    modifier =
                                        Modifier.size(
                                            IconButtonDefaults.smallContainerSize(
                                                widthOption =
                                                    IconButtonDefaults.IconButtonWidthOption.Wide
                                            )
                                        ),
                                ) {
                                    Icon(
                                        imageVector = vectorResource(pair.second),
                                        contentDescription = null,
                                        modifier = Modifier.size(IconButtonDefaults.smallIconSize),
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}