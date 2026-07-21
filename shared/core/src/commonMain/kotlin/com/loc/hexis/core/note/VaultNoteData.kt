/*
 * Copyright (C) 2024 Hexis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.loc.hexis.core.note

import kotlinx.serialization.Serializable

@Serializable
data class VaultNote(
    val entries: List<VaultEntry> = emptyList()
)

@Serializable
data class VaultEntry(
    val id: String,
    val label: String,
    val value: String,
    val notes: String = "",
)
