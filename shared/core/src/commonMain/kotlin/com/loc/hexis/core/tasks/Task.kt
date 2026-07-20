package com.loc.hexis.core.tasks

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Model for Tasks
 *
 * @param categoryId : Id of the category the task belongs to
 * @param title : the content of the task
 * @param status : task completion status
 * @param index : used for sorting in UI
 * @param reminder : [LocalDateTime] if reminder is set
 */
@Serializable
data class Task(
    val id: Long = 0,
    val categoryId: Long,
    val title: String,
    val description: String = "",
    val index: Int = 0,
    val status: Boolean = false,
    val reminder: LocalDateTime? = null,
)
