package com.loc.hexis.shared.ui.habit.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hexis.shared.ui.generated.resources.*
import org.jetbrains.compose.resources.painterResource

@Composable
fun CardArrows(
    onBackAction: () -> Unit,
    onForwardAction: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onExpandAction: (() -> Unit)? = null,
) {
    Row(modifier = modifier) {
        IconButton(onClick = onBackAction, enabled = enabled) {
            Icon(
                painter = painterResource(Res.drawable.arrow_back),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        IconButton(onClick = onForwardAction, enabled = enabled) {
            Icon(
                painter = painterResource(Res.drawable.arrow_forward),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        onExpandAction?.let {
            FilledTonalIconButton(
                onClick = onExpandAction,
                shapes = IconButtonDefaults.shapes(),
                enabled = enabled,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.expand),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
