
package com.loc.hexis.shared.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.loc.hexis.core.getLogicalToday
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate

@Composable
fun rememberToday(isCutoffEnabled: Boolean = false, cutoffHour: Int = 4): State<LocalDate> {
    val dateState =
        remember(isCutoffEnabled, cutoffHour) {
            mutableStateOf(getLogicalToday(isCutoffEnabled, cutoffHour))
        }
    LaunchedEffect(isCutoffEnabled, cutoffHour) {
        while (true) {
            delay(30_000)
            val newDate = getLogicalToday(isCutoffEnabled, cutoffHour)
            if (newDate != dateState.value) {
                dateState.value = newDate
            }
        }
    }
    return dateState
}
