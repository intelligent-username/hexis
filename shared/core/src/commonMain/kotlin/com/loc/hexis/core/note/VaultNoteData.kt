
package com.loc.hexis.core.note

import kotlinx.serialization.Serializable

@Serializable data class VaultNote(val entries: List<VaultEntry> = emptyList())

@Serializable
data class VaultEntry(val id: String, val label: String, val value: String, val notes: String = "")
