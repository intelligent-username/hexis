package com.loc.hexis.core.tasks

import kotlinx.serialization.Serializable

@Serializable
/**
 * Task Category the color parameter is unused, waiting for its day like the pink suit in joji's
 * basement
 */
data class Category(val id: Long = 0, val name: String, val index: Int = 0, val color: String)
