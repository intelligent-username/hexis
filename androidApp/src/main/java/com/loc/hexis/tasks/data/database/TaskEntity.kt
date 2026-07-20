package com.loc.hexis.tasks.data.database

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlinx.datetime.LocalDateTime

@Entity(
    tableName = "task",
    foreignKeys =
        [
            ForeignKey(
                entity = CategoryEntity::class,
                parentColumns = ["id"],
                childColumns = ["categoryId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices = [Index(value = ["categoryId"])],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val title: String,
    val status: Boolean = false,
    val index: Int = 0,
    @ColumnInfo(name = "reminder", defaultValue = "NULL") val reminder: LocalDateTime? = null,
)
