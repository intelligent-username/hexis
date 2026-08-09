
package com.loc.hexis.core.habits

enum class WeeklyTimePeriod {
    DAYS_7,
    MONTHS_2,
    MONTHS_6,
    YEARS_1;

    companion object {
        fun WeeklyTimePeriod.toWeeks(): Int {
            return when (this) {
                DAYS_7 -> 1
                MONTHS_2 -> 8
                MONTHS_6 -> 26
                YEARS_1 -> 52
            }
        }

        fun WeeklyTimePeriod.toDisplayString(): String {
            return when (this) {
                DAYS_7 -> "7D"
                MONTHS_2 -> "2M"
                MONTHS_6 -> "6M"
                YEARS_1 -> "1Y"
            }
        }
    }
}
