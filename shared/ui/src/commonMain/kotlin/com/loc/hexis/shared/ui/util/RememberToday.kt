package com.loc.hexis.shared.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.loc.hexis.core.now
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate

@Composable
fun rememberToday(): State<LocalDate> {
    val dateState = remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            val newDate = LocalDate.now()
            if (newDate != dateState.value) {
                dateState.value = newDate
            }
        }
    }
    return dateState
}
