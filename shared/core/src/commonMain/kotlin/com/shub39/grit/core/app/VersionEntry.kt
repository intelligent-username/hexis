package com.shub39.grit.core.app

import kotlinx.serialization.Serializable

@Serializable data class VersionEntry(val version: String, val changes: List<String>)

typealias Changelog = List<VersionEntry>