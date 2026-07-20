package com.loc.hexis.habits.data.database

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "habit_index")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val index: Int,
    val days: Set<DayOfWeek>,
    val time: LocalDateTime,
    @ColumnInfo(name = "reminder", defaultValue = "1") val reminder: Boolean,
    @ColumnInfo(defaultValue = "CHECKBOX") val displayMode: String,
    @ColumnInfo(defaultValue = "1.0") val targetValue: Double?,
    @ColumnInfo(defaultValue = "0") val pomodoroLinked: Boolean,
    @ColumnInfo(defaultValue = "1.0") val incrementBy: Double,
)
