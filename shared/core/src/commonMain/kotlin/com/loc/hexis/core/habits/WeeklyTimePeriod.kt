package com.loc.hexis.core.habits

enum class WeeklyTimePeriod {
    MONTHS_2,
    MONTHS_4,
    MONTHS_8,
    YEARS_1;

    companion object {
        fun WeeklyTimePeriod.toWeeks(): Int {
            return when (this) {
                MONTHS_2 -> 8
                MONTHS_4 -> 16
                MONTHS_8 -> 32
                YEARS_1 -> 52
            }
        }

        fun WeeklyTimePeriod.toDisplayString(): String {
            return when (this) {
                MONTHS_2 -> "2M"
                MONTHS_4 -> "4M"
                MONTHS_8 -> "8M"
                YEARS_1 -> "1Y"
            }
        }
    }
}
