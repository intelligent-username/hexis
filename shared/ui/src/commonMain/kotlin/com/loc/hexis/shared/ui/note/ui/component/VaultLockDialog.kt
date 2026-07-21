/*
 * Copyright (C) 2026 Hexis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.loc.hexis.shared.ui.note.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.hashPassword
import com.loc.hexis.shared.ui.components.HexisDialog
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.lock
import org.jetbrains.compose.resources.vectorResource

@Composable
fun VaultLockDialog(
    storedPasswordHash: String?,
    onUnlocked: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    var inputPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    HexisDialog(onDismissRequest = onDismissRequest) {
        Icon(
            imageVector = vectorResource(Res.drawable.lock),
            contentDescription = "Vault Locked",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp),
        )

        Text(
            text = "Unlock Vault",
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
        )

        Text(
            text = "Enter your vault password to access this note.",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = flexFontRounded()),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = inputPassword,
            onValueChange = {
                inputPassword = it
                errorMessage = null
            },
            placeholder = { Text("Vault Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel", fontFamily = flexFontRounded())
            }
            Button(
                onClick = {
                    if (storedPasswordHash.isNullOrBlank()) {
                        // No password hash stored, allow access
                        onUnlocked()
                    } else {
                        val enteredHash = hashPassword(inputPassword.trim())
                        if (enteredHash == storedPasswordHash) {
                            onUnlocked()
                        } else {
                            errorMessage = "Incorrect password"
                        }
                    }
                }
            ) {
                Text("Unlock", fontFamily = flexFontRounded())
            }
        }
    }
}
