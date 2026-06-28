package com.loc.hexis.shared.ui.habit.ui.component

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loc.hexis.shared.ui.blurPossible
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Wrapper card for analytics display */
@Composable
fun AnalyticsCard(
    title: String,
    icon: DrawableResource,
    modifier: Modifier = Modifier,
    header: @Composable (RowScope.() -> Unit) = {},
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            modifier =
                Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                    .heightIn(min = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                fontFamily = flexFontRounded(),
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).basicMarquee(),
            )

            header()
        }

        content()
    }
}