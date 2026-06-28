package com.loc.hexis.tasks.data.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val index: Int = 0,
    val color: String,
)