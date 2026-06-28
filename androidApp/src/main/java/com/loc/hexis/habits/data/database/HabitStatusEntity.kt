package com.loc.hexis.habits.data.database

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "habit_status",
    foreignKeys =
        [
            ForeignKey(
                entity = HabitEntity::class,
                parentColumns = ["id"],
                childColumns = ["habitId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices = [Index(value = ["habitId"])],
)
data class HabitStatusEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: LocalDate,
    @ColumnInfo(defaultValue = "1.0") val value: Double,
)